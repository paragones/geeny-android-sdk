package io.geeny.sdk.geeny.flow

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.geeny.things.BleThing
import io.geeny.sdk.geeny.things.ResourceMethod
import io.geeny.sdk.geeny.things.TheResource
import io.geeny.sdk.geeny.things.TheThingType
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

class Flower(
        private val configuration: GeenyConfiguration,
        private val store: KeyValueStore,
        private val router: Router) {


    fun getOrCreateRoutes(bleThing: BleThing, thingType: TheThingType): Observable<List<GeenyFlow>> =
            Observable.fromIterable(thingType.resources)
                    .flatMap {
                        getOrCreateRoutes(bleThing, it)
                    }
                    .toList().toObservable()
                    .doOnNext { GLog.d(TAG, "Loaded ${it.size} flows.") }


    private fun getOrCreateRoutes(bleThing: BleThing, theResource: TheResource): Observable<GeenyFlow> =

            when (theResource.method) {

                ResourceMethod.PUB -> {
                    GLog.d(TAG, "Creating producer route for ${theResource.uri}")
                    val route = RouteInfo(
                            RouteType.BLE,
                            Direction.PRODUCER,
                            theResource.uri.toLowerCase(),
                            bleThing.deviceInfo.address,
                            theResource.uri.toLowerCase()
                    )
                    router.get(route.identifier())
                            .switchIfEmpty(router.create(
                                    RouteType.BLE,
                                    Direction.PRODUCER,
                                    theResource.uri.toLowerCase(),
                                    bleThing.deviceInfo.address,
                                    theResource.uri.toLowerCase(),
                                    TopicJournalType.DISMISS_ON_READ
                            ))
                            .flatMap {
                                GLog.d(TAG, "Creating consumer route for ${theResource.uri}")
                                Observable.zip(
                                        Observable.just(it),
                                        router.getOrCreate(RouteType.MQTT,
                                                Direction.CONSUMER,
                                                theResource.uri.toLowerCase(),
                                                MqttConfig(configuration.environment.geenyMqttBrokerUrl(), bleThing.thing.id, false).id(),
                                                theResource.uri.toLowerCase()
                                        ),
                                        BiFunction<Route, Route, GeenyFlow> { producerRoute, consumerRoute -> GeenyFlow(listOf(producerRoute, consumerRoute)) }
                                )
                            }
                }
                ResourceMethod.SUB -> {
                    GLog.d(TAG, "Creating producer route for ${theResource.uri}")
                    router.getOrCreate(RouteType.MQTT,
                            Direction.PRODUCER,
                            theResource.uri.toLowerCase(),
                            MqttConfig(configuration.environment.geenyMqttBrokerUrl(), bleThing.thing.id, false).id(),
                            theResource.uri.toLowerCase(),
                            TopicJournalType.OVERWRITE
                    ).flatMap {
                        GLog.d(TAG, "Creating consumer route for ${theResource.uri}")
                        Observable.zip(
                                Observable.just(it),
                                router.getOrCreate(RouteType.BLE,
                                        Direction.CONSUMER,
                                        theResource.uri.toLowerCase(),
                                        bleThing.deviceInfo.address,
                                        theResource.uri.toLowerCase()
                                ),
                                BiFunction<Route, Route, GeenyFlow> { producerRoute, consumerRoute -> GeenyFlow(listOf(producerRoute, consumerRoute)) }
                        )
                    }
                }
                else -> Observable.error<GeenyFlow>(Exception("Resource method unknown!"))
            }


    fun onInit(result: SdkInitializationResult): ObservableSource<SdkInitializationResult> {
        return Observable.just(result)
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
    }

    companion object {
        val TAG = Flower::class.java.simpleName
    }
}