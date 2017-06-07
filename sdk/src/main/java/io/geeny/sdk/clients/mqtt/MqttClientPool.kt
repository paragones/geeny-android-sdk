package io.geeny.sdk.clients.mqtt

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.mqtt.repository.MqttRepository
import io.geeny.sdk.common.GLog
import io.geeny.sdk.geeny.cloud.api.repos.Certificate
import io.geeny.sdk.geeny.cloud.api.repos.certificate.CertificateRepository
import io.reactivex.Observable
import java.util.HashMap

class MqttClientPool(
        private val configuration: GeenyConfiguration,
        private val context: Context,
        private val mqttRepository: MqttRepository,
        private val certificateRepository: CertificateRepository) {

    val map: MutableMap<String, GeenyMqttClient> = HashMap()
    private fun toList(): List<GeenyMqttClient> = map.toList().map { it.second }

    fun availableClients(): Observable<List<GeenyMqttClient>> = Observable.just(toList())

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .map {
                    GLog.i(TAG, "Creating mqtt clients from third parties")
                    val clients = configuration.mqttConfigs
                    clients
                            .map { GeenyMqttClient(it, context) }
                            .forEach {
                                map.put(it.mqttConfig.id(), it)
                                result.addMqttConfig(it.mqttConfig)
                            }
                    result
                }
                .flatMap { r ->
                    GLog.d(TAG, "Loading persisted clients")
                    loadConfigurations()
                            .map {
                                GLog.d(TAG, "Loaded client $it")
                                map.put(it.id(), GeenyMqttClient(it, context))
                                r.addMqttConfig(it)
                            }.toList().toObservable().map { r }
                }
    }


    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
    }

    fun get(serverUri: String): Observable<GeenyMqttClient> = Observable.create { subscriber ->
        if (map.containsKey(serverUri)) {
            subscriber.onNext(map[serverUri]!!)
        }

        subscriber.onComplete()
    }

    fun createGeenyClient(thingId: String, certificate: Certificate?): Observable<GeenyMqttClient> =
            Observable.just(MqttConfig(configuration.environment.geenyMqttBrokerUrl(), thingId, certificate != null, certificate))
                    .flatMap { saveConfiguration(it) }
                    .map { GeenyMqttClient(it, context) }
                    .doOnNext { map[it.mqttConfig.id()] = it }


    private fun saveConfiguration(mqttConfig: MqttConfig): Observable<MqttConfig> {
        return if (mqttConfig.isSecure) {
            certificateRepository.save(mqttConfig.certificate!!, mqttConfig.clientId)
                    .flatMap { mqttRepository.save(mqttConfig) }
        } else {
            mqttRepository.save(mqttConfig)
        }
    }

    private fun loadConfigurations(): Observable<MqttConfig> =
            mqttRepository.list()
                    .flatMapIterable { it }
                    .flatMap { config ->
                        if (config.isSecure) {
                            certificateRepository.load(config.clientId)
                                    .map { MqttConfig(config.serverUri, config.clientId, true, it) }
                        } else {
                            Observable.just(config)
                        }
                    }

    companion object {
        val TAG = MqttClientPool::class.java.simpleName
    }
}