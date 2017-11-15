package io.geeny.sdk.clients.mqtt

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.mqtt.repository.MqttConfigJsonConverter
import io.geeny.sdk.clients.mqtt.repository.MqttRepository
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.cloud.api.repos.SimpleCache
import io.geeny.sdk.geeny.cloud.api.repos.certificate.CertificateRepository
import io.geeny.sdk.geeny.things.Thing
import io.reactivex.Observable

class MqttComponent(
        private val configuration: GeenyConfiguration,
        private val keyValueStore: KeyValueStore,
        private val context: Context) {

    val pool: MqttClientPool by lazy {
        MqttClientPool(
                configuration,
                context,
                MqttRepository(object : SimpleCache<MqttConfig>() {
                    override fun id(t: MqttConfig): String = t.id()
                },
                        ListDisk(keyValueStore, MqttConfigJsonConverter, "MQTT_CONFIG_LIST_ID")),
                CertificateRepository(context)
        )
    }

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .flatMap { pool.onInit(it) }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap { pool.onTearDown(it) }
    }

    /*
        API
     */
    fun list(): Observable<List<GeenyMqttClient>> = pool.availableClients()

    fun get(serverUri: String): Observable<GeenyMqttClient> = pool.get(serverUri)

    fun create(thing: Thing): Observable<Thing> =
            pool.createGeenyClient(thing.cloudThingInfo.id, thing.cloudThingInfo.certs)
                    .map { thing }

}