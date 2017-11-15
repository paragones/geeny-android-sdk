package io.geeny.sdk.routing.bote

import io.geeny.sdk.routing.bote.topicjournal.BoteResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class BoteConsumer(val broker: BoteBroker,
                            val topic: String,
                            private val delay: Long = 2000L,
                            private val executorService: ExecutorService = Executors.newSingleThreadExecutor()) : Runnable {


    private var cancel: Boolean = true

    fun startLoop() {
        cancel = false
        executorService.execute(this)
    }

    fun stopLoop() {
        cancel = true
    }

    override fun run() {
        var iterator: String? = null
        while (!cancel) {
            val response = broker.read(topic, iterator)
            iterator = response.lastIterator
            onResponse(response)
            Thread.sleep(delay)
        }
    }

    abstract fun onResponse(response: BoteResponse)

    abstract fun onError(t: Throwable)

    companion object {
        val TAG = BoteConsumer::class.java.simpleName
    }
}