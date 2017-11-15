package io.geeny.sdk.clients

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.ble.BleComponent
import io.geeny.sdk.clients.custom.CustomClientComponent
import io.geeny.sdk.clients.mqtt.MqttComponent
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.reactivex.Observable

class ClientsComponent(
        private val configuration: GeenyConfiguration,
        private val keyValueStore: KeyValueStore,
        private val context: Context) {

    val ble: BleComponent by lazy {
        BleComponent(configuration, keyValueStore, context)
    }

    val mqtt: MqttComponent by lazy {
        MqttComponent(configuration, keyValueStore, context)
    }

    val custom: CustomClientComponent by lazy {
        CustomClientComponent(configuration)
    }

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .flatMap { ble.onInit(it) }
                .doOnNext { GLog.d(GeenySdk.TAG, "ble Clients initialized") }
                .flatMap { mqtt.onInit(it) }
                .doOnNext { GLog.d(GeenySdk.TAG, "mqtt Clients initialized") }
                .flatMap { custom.onInit(it) }
                .doOnNext { GLog.d(GeenySdk.TAG, "Custom Clients initialized") }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap { custom.onTearDown(it) }
                .flatMap { mqtt.onTearDown(it) }
                .flatMap { ble.onTearDown(it) }
    }
}