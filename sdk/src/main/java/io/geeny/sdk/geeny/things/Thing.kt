package io.geeny.sdk.geeny.things

import io.geeny.sdk.geeny.cloud.api.repos.*
import java.util.*

data class Thing(
        val localThingInfo: LocalThingInfo,
        val cloudThingInfo: CloudThingInfo
) {
    override fun toString(): String =
            StringBuilder()
                    .append(localThingInfo.toString())
                    .append("\n")
                    .append(cloudThingInfo.toString())
                    .toString()
}



data class LocalThingInfo(
        val deviceName: String,
        val address: String,
        val protocolVersion: Int,
        val serialNumber: String,
        val thingTypeId: String
) {
    fun isEmpty(): Boolean = address.isEmpty()
}

val emptyBleThing = Thing(emptyDeviceInfo(), emptyThing())

fun emptyDeviceInfo(): LocalThingInfo = LocalThingInfo("", "", -1, "", "")

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

