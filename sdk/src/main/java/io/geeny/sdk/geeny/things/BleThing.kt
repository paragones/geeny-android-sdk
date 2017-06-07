package io.geeny.sdk.geeny.things

import io.geeny.sdk.geeny.cloud.api.repos.*
import java.util.*

data class BleThing(
        val deviceInfo: DeviceInfo,
        val thing: Thing
) {
    override fun toString(): String =
            StringBuilder()
                    .append(deviceInfo.toString())
                    .append("\n")
                    .append(thing.toString())
                    .toString()

}


val emptyBleThing = BleThing(emptyDeviceInfo(), emptyThing())

fun emptyDeviceInfo(): DeviceInfo = DeviceInfo("", "", -1, UUID.randomUUID(), UUID.randomUUID())

class TheThingType(val thingType: ThingType, val resources: List<TheResource>)


data class TheResource(val id: String,
                       val uri: String,
                       val method: ResourceMethod,
                       val messageType: MessageType)

enum class ResourceMethod(value: String) {
    UNKNOWN("unknown"),
    PUB("pub"),
    SUB("sub");

    companion object {
        fun from(value: String): ResourceMethod = when (value) {
            "sub" -> SUB
            "pub" -> PUB
            else -> UNKNOWN
        }

    }
}

