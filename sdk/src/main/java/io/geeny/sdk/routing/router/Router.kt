package io.geeny.sdk.routing.router

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.BleClientPool
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.CustomClientPool
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.clients.mqtt.MqttClientPool
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.common.SimpleCache
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import io.geeny.sdk.routing.router.ble.BleConsumerRoute
import io.geeny.sdk.routing.router.ble.BleProducerRoute
import io.geeny.sdk.routing.router.custom.CustomConsumerRoute
import io.geeny.sdk.routing.router.custom.CustomProducerRoute
import io.geeny.sdk.routing.router.mqtt.MqttConsumerRoute
import io.geeny.sdk.routing.router.mqtt.MqttProducerRoute
import io.geeny.sdk.routing.router.types.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction


class Router(
        configuration: GeenyConfiguration,
        store: KeyValueStore,
        val broker: BoteBroker,
        mqttClientPool: MqttClientPool,
        bleClientPool: BleClientPool,
        customClientPool: CustomClientPool) {

    private val cache: SimpleCache<Route> = object : SimpleCache<Route>() {
        override fun id(value: Route): String = value.identifier()
    }

    private val disk: ListDisk<RouteInfo> = RouterDisk(store)
    private val factory: RouteFactory = RouteFactory(mqttClientPool, bleClientPool, customClientPool, broker)

    fun onInit(result: SdkInitializationResult): ObservableSource<SdkInitializationResult> {
        return Observable.just(result)
                .doOnNext { GLog.d(TAG, "initializing router.....") }
                .flatMap {
                    GLog.d(TAG, "restoring routes...")
                    restore().map { result.copy(numberOfRoutesLoaded = it.size) }
                }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .doOnNext { GLog.d(TAG, "tearing down router.....") }
                .flatMap {
                    cache.clear().andThen(Observable.just(it))
                }
    }

    private fun restore(): Observable<List<Route>> =
            disk.list()
                    .flatMapIterable { it }    // turn list into stream
                    .flatMap {
                        GLog.d(TAG, "Creating route $it")
                        factory.createRoute(it)
                    }
                    .flatMap {
                        GLog.d(TAG, "Created route $it")
                        cache.save(it)
                    }
                    .toList().toObservable()// turn stream to list again


    private fun save(route: Route): Observable<Route> =
            cache.save(route)
                    .flatMap {
                        val r = it
                        disk.save(it.info()).map { r }
                    }


    /*
        API
     */
    fun getOrCreate(type: RouteType, direction: Direction, topic: String, address: String, uuid: String, topicJournalType: TopicJournalType = TopicJournalType.OVERWRITE): Observable<Route> =
            cache.get(RouteInfo(type, direction, topic, address, uuid).identifier())
                    .switchIfEmpty(create(type, direction, topic, address, uuid, topicJournalType))


    fun create(type: RouteType, direction: Direction, topic: String, address: String, uuid: String, topicJournalType: TopicJournalType = TopicJournalType.OVERWRITE): Observable<Route> {
        return factory.createRoute(RouteInfo(type, direction, topic, address, uuid))
                .doOnNext { GLog.d(TAG, "Route created $it") }
                .flatMap { save(it) }
                .flatMap { route ->
                    if (direction == Direction.PRODUCER) {
                        broker.createTopic(route.info().topic, topicJournalType).doOnNext { GLog.d(TAG, "Topic created $it") }.map { route }
                    } else {
                        Observable.just(route)
                    }
                }
    }

    fun remove(route: RouteInfo): Observable<RouteInfo> =
            cache.remove(route.identifier())
                    .map {
                        it.stop()
                        it
                    }
                    .flatMap { disk.remove(it.info()) }
                    .flatMap {
                        val info = it
                        val topic = it.topic
                        // load all routes with topic and when empty delete the topic on the broker
                        if (info.direction == Direction.CONSUMER) {
                            Observable.just(info)
                        } else {
                            broker.removeTopic(topic).map { info }
                        }
                    }
                    .flatMap {
                        val info = it
                        if (info.direction == Direction.CONSUMER) {
                            Observable.just(info)
                        } else {
                            loadRoutesWithTopic(it.topic)
                                    .flatMapIterable { it }
                                    .flatMap { remove(it) }
                                    .map { info }
                                    .defaultIfEmpty(info)
                        }
                    }

    fun list(): Observable<List<Route>> = cache.list()
    fun get(type: RouteType, address: String, uuid: String, direction: Direction): Observable<Route> = get(identifier(address, type, direction, uuid)).defaultIfEmpty(EmptyRoute(direction = direction))
    fun get(id: String): Observable<Route> = cache.get(id)

    fun loadRoutesWithTopic(topic: String): Observable<List<RouteInfo>> =
            list()
                    .flatMapIterable { it }
                    .map { it.info() }
                    .filter { it.topic == topic }
                    .map { it }
                    .toList()
                    .toObservable()

    companion object {
        val TAG = Router::class.java.simpleName
        fun identifier(address: String, type: RouteType, direction: Direction, clientResourceId: String) = "${address}_${type}_${direction}_$clientResourceId"
    }
}

class RouteFactory(
        private val mqttClientPool: MqttClientPool,
        private val bleClientPool: BleClientPool,
        private val customClientPool: CustomClientPool,
        private val broker: BoteBroker) {
    fun createRoute(info: RouteInfo): Observable<Route> = when (info.type) {
        RouteType.EMPTY -> Observable.just(EmptyRoute())
        RouteType.MQTT -> createMqttRoute(info)
        RouteType.BLE -> createBleRoute(info)
        RouteType.CUSTOM -> createCustomRoute(info)
    }

    private fun createMqttRoute(info: RouteInfo): Observable<Route> =
            Observable.zip<RouteInfo, GeenyMqttClient, Route>(
                    Observable.just(info),
                    mqttClientPool.get(info.clientIdentifier),
                    BiFunction { i, c ->
                        when (i.direction) {
                            Direction.PRODUCER -> MqttProducerRoute(i, broker, c)
                            Direction.CONSUMER -> MqttConsumerRoute(i, broker, c)
                        }
                    }
            )

    private fun createCustomRoute(info: RouteInfo): Observable<Route> =
            Observable.zip<RouteInfo, Client, Route>(
                    Observable.just(info),
                    customClientPool.get(info.clientIdentifier),
                    BiFunction { i, c ->
                        when (i.direction) {
                            Direction.PRODUCER -> CustomProducerRoute(i, broker, c)
                            Direction.CONSUMER -> CustomConsumerRoute(i, broker, c)
                        }
                    }
            )

    private fun createBleRoute(info: RouteInfo): Observable<Route> =
            bleClientPool.getOrCreate(info.clientIdentifier).map {
                when (info.direction) {
                    Direction.PRODUCER -> BleProducerRoute(info, it, broker)
                    Direction.CONSUMER -> BleConsumerRoute(info, it, broker)
                }
            }
}