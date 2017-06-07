package io.geeny.sdk.routing.bote.topicjournal

class ByteArrayQueue: Queue<ByteArray>() {
    override fun valueSize(value: ByteArray): Int = value.size
}