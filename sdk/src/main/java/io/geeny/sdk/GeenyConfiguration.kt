package io.geeny.sdk

import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.environment.DevelopmentEnvironment
import io.geeny.sdk.common.environment.Environment

class GeenyConfiguration private constructor(
        val environment: Environment,
        val mqttConfigs: List<MqttConfig>,
        val interceptors: Map<String, Slot>,
        val applicationConfiguration: ApplicationConfiguration) {
    class Builder {

        private val mqttConfigs: MutableList<MqttConfig> = ArrayList()
        private val applicationSlots: MutableMap<String, Slot> = HashMap()
        private var environmentType = Environment.Type.DEVELOPMENT
        private var clientSecret: String = ""
        private var clientId: String = ""

        private val environment by lazy {
            when (environmentType) {
                Environment.Type.PRODUCTION -> TODO()
                Environment.Type.DEVELOPMENT -> DevelopmentEnvironment()
            }
        }

        fun withMqtt(mqttConfig: MqttConfig): Builder {
            mqttConfigs.add(mqttConfig)
            return this
        }

        fun withSlot(slotId: String, slot: Slot): Builder {
            applicationSlots.put(slotId, slot)
            return this
        }

        fun withClientSecret(clientSecret: String): Builder {
            this.clientSecret = clientSecret
            return this
        }

        fun withClientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }

        fun build(): GeenyConfiguration {
            return GeenyConfiguration(
                    environment,
                    mqttConfigs,
                    applicationSlots,
                    ApplicationConfiguration(clientSecret, clientId)
            )
        }
    }
}

data class ApplicationConfiguration(val clientSecret: String, val clientId: String)

