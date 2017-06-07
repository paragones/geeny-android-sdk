package io.geeny.sdk.routing.bote.topicjournal

import com.github.daemontus.isSome
import com.github.daemontus.unwrap
import com.github.daemontus.unwrapOr
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class DismissOnReadTopicJournal(val info: TopicInfo) : TopicJournal {
    override fun info(): TopicInfo = info
    override fun type(): TopicJournalType = TopicJournalType.DISMISS_ON_READ

    private val mutex: Lock = ReentrantLock()
    private var currentIterator: Iterator? = null
    private val queue: Queue<ByteArray> = ByteArrayQueue()

    override fun send(request: BoteRequest): BoteResponse =
            when (request.messageType) {
                MessageType.CAST -> cast(request)
                MessageType.CALL -> TODO()
                MessageType.SUBSCRIBE -> TODO()
                MessageType.ACK -> TODO()
                MessageType.READ -> read(request)
                MessageType.EMPTY -> emptyBoteResponse()
            }


    private fun read(request: BoteRequest): BoteResponse {
        var response: BoteResponse = emptyBoteResponse()
        mutex.lock()

        // process iterator
        if (currentIterator == null) {
            currentIterator = createIterator(151)
        } else {
            // somebody is already consuming, but this one
            // doesnt have a iterator...it cant be him
            if (request.lastIterator == null) {
                mutex.unlock()
                return response
            }
            // this one has an iterator, but is it the same?
            if (currentIterator?.id != request.lastIterator) {
                mutex.unlock()
                return response
            }
        }

        val value = queue.pop()
        return if(value.isSome()) {
            mutex.unlock()
            BoteResponse(MessageType.READ, value.unwrap(), currentIterator!!.id)
        } else{
            mutex.unlock()
            BoteResponse(MessageType.EMPTY, null, currentIterator!!.id)
        }

    }


    private fun cast(request: BoteRequest): BoteResponse {
        var response: BoteResponse = emptyBoteResponse()
        mutex.lock()

        queue.push(request.payload!!)

        while (queue.memorySize > info.maxCacheSize) {
            queue.pop()
        }

        mutex.unlock()
        return response
    }
}