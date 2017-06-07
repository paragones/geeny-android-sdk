package io.geeny.sdk.routing

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.ble.BleClientPool
import io.geeny.sdk.clients.custom.CustomClientPool
import io.geeny.sdk.clients.mqtt.MqttClientPool
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.geeny.things.TheThingType
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.geeny.flow.Flower
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.Route
import io.reactivex.Observable

class RoutingComponent(
        private val configuration: GeenyConfiguration,
        mqttClientPool: MqttClientPool,
        private val pool: BleClientPool,
        private val customClientPool: CustomClientPool,
        keyValueStore: KeyValueStore) {

    val broker: BoteBroker by lazy {
        BoteBroker(keyValueStore)
    }

    val router: Router by lazy {
        Router(configuration, keyValueStore, broker, mqttClientPool, pool, customClientPool)
    }

    val flower: Flower by lazy {
        Flower(configuration, keyValueStore, router)
    }

    fun onInit(sdkInitializationResult: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(sdkInitializationResult)
                .flatMap { broker.onInit(it) }
                .doOnNext { GLog.d(GeenySdk.TAG, "broker initialized") }
                .flatMap { router.onInit(it) }
                .doOnNext { GLog.d(GeenySdk.TAG, "router initialized") }
                .flatMap { flower.onInit(it) }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap { flower.onTearDown(it) }
                .flatMap { router.onTearDown(it) }
                .flatMap { broker.onTearDown(it) }
    }
}