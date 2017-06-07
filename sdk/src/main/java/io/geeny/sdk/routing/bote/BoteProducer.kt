package io.geeny.sdk.routing.bote

import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.toHex
import java.nio.charset.Charset

open class BoteProducer(val broker: BoteBroker, val topic: String) {

    fun send(array: ByteArray) {
        val response = broker.send(topic, array)
        GLog.d(TAG, "Sent " + array.toHex() +"  Received "+ response)
    }

    fun send(s: String) {
        send(s.toByteArray(Charset.forName("UTF-8")))
    }

    companion object {
        val TAG = BoteProducer::class.java.simpleName
    }
}