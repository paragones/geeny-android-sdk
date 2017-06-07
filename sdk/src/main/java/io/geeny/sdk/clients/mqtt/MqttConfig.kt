package io.geeny.sdk.clients.mqtt

import io.geeny.sdk.geeny.cloud.api.repos.Certificate

data class MqttConfig(val serverUri: String, val clientId: String, val isSecure: Boolean, val certificate: Certificate? = null) {
    fun id(): String = clientId
}