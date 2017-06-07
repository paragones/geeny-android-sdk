package io.geeny.sdk.clients.mqtt.repository

import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.ListDisk
import io.reactivex.Observable
import org.json.JSONObject

class MqttRepository(
        private val cache: io.geeny.sdk.geeny.cloud.api.repos.SimpleCache<MqttConfig>,
        private val disk: ListDisk<MqttConfig>
) {
    fun save(mqttConfig: MqttConfig): Observable<MqttConfig> =
            disk.save(mqttConfig)
                    .flatMap { cache.save(it) }
                    .map { mqttConfig }

    fun list(): Observable<List<MqttConfig>> =
            disk.list()
                    .flatMap { cache.save(it) }
}

object MqttConfigJsonConverter : JSONConverter<MqttConfig> {
    override fun id(value: MqttConfig): String = value.id()

    override fun toJSON(value: MqttConfig): JSONObject =
            JSONObject().apply {
                put(JSON_KEY_SERVER_URI, value.serverUri)
                put(JSON_KEY_CLIENT_ID, value.clientId)
                put(JSON_KEY_IS_SECURE, value.isSecure)
            }


    override fun fromJSON(json: JSONObject): MqttConfig =
            MqttConfig(
                    json.getString(JSON_KEY_SERVER_URI),
                    json.getString(JSON_KEY_CLIENT_ID),
                    json.getBoolean(JSON_KEY_IS_SECURE)
            )

    private val JSON_KEY_SERVER_URI = "JSON_KEY_SERVER_URI"
    private val JSON_KEY_CLIENT_ID = "JSON_KEY_CLIENT_ID"
    private val JSON_KEY_IS_SECURE = "JSON_KEY_IS_SECURE"
}