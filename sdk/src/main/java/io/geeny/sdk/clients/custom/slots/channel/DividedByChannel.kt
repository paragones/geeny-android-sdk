package io.geeny.sdk.clients.custom.slots.channel

import io.geeny.sdk.common.TypeConverters

class DividedByChannel(resourceId: String, val divisor: Int) : MapChannel(resourceId) {
    override fun map(array: ByteArray): ByteArray {
        val original = TypeConverters.bytesToInt(array)
        return TypeConverters.intToBytes(original / divisor)
    }

    override fun name(): String = "Divided By $divisor"
}