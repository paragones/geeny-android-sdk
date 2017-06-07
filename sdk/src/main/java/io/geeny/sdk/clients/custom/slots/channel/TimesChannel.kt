package io.geeny.sdk.clients.custom.slots.channel

import io.geeny.sdk.common.TypeConverters

class TimesChannel(resourceId: String, val factor: Int) : MapChannel(resourceId) {
    override fun map(array: ByteArray): ByteArray {
        val original = TypeConverters.bytesToInt(array)
        return TypeConverters.intToBytes(factor * original)
    }

    override fun name(): String = "Multiply by $factor"
}