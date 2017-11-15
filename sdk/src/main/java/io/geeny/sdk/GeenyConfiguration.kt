package io.geeny.sdk

import io.geeny.sdk.clients.custom.VirtualThing
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.environment.DevelopmentEnvironment
import io.geeny.sdk.common.environment.Environment
import io.geeny.sdk.common.environment.ProductionEnvironment

class GeenyConfiguration private constructor(
        val environment: Environment,
        val mqttConfigs: List<MqttConfig>,
        val slots: Map<String, Slot>,
        val virtualThings: List<VirtualThing>,
        val applicationConfiguration: ApplicationConfiguration) {
    class Builder {

        private val mqttConfigs: MutableList<MqttConfig> = ArrayList()
        private val applicationSlots: MutableMap<String, Slot> = HashMap()
        private var environmentType = Environment.Type.DEVELOPMENT
        private var clientSecret: String = ""
        private var clientId: String = ""
        private var virtualThings: MutableList<VirtualThing> = ArrayList()

        private val environment by lazy {
            when (environmentType) {
                Environment.Type.PRODUCTION -> ProductionEnvironment()
                Environment.Type.DEVELOPMENT -> DevelopmentEnvironment()
            }
        }

        fun withEnvironment(type: Environment.Type): Builder {
            this.environmentType = type
            return this
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

        fun withVirtualThing(virtualThing: VirtualThing): Builder {
            virtualThings.add(virtualThing)
            return this
        }

        fun build(): GeenyConfiguration {
            return GeenyConfiguration(
                    environment,
                    mqttConfigs,
                    applicationSlots,
                    virtualThings,
                    ApplicationConfiguration(clientSecret, clientId)
            )
        }
    }
}

data class ApplicationConfiguration(val clientSecret: String, val clientId: String)

