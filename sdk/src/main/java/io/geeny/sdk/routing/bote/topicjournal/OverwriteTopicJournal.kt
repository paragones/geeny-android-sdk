package io.geeny.sdk.routing.bote.topicjournal

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class OverwriteTopicJournal(val info: TopicInfo) : TopicJournal {
    private var theValue: ByteArray? = null
    private val mutex: Lock = ReentrantLock()

    override fun info(): TopicInfo = info
    override fun type(): TopicJournalType = TopicJournalType.OVERWRITE
    private var currentIterator: Iterator? = null

    override fun send(request: BoteRequest): BoteResponse {
        when (request.messageType) {
            MessageType.CAST -> return cast(request)
            MessageType.CALL -> TODO()
            MessageType.SUBSCRIBE -> TODO()
            MessageType.ACK -> TODO()
            MessageType.READ -> return read(request)
            else -> return emptyBoteResponse()
        }

    }

    private fun read(request: BoteRequest): BoteResponse {
        mutex.lock()

        // no values yet
        if (currentIterator == null) {
            mutex.unlock()
            return emptyBoteResponse()
        } else if (request.lastIterator != null) {
            // this one has an iterator, but if it is the same, no need to update
            if (currentIterator?.id == request.lastIterator) {
                mutex.unlock()
                return emptyBoteResponseWithIterator(request.lastIterator)
            }
        }

        // value has changed -> send value
        mutex.unlock()
        return BoteResponse(MessageType.READ, theValue, currentIterator!!.id)
    }


    private fun cast(request: BoteRequest): BoteResponse {
        var response: BoteResponse = emptyBoteResponse()
        mutex.lock()

        currentIterator = createIterator(151)

        if (theValue == null) {
            theValue = request.payload!!
        } else if (theValue != request.payload!!) {
            theValue = request.payload!!
        }
        mutex.unlock()
        return response
    }
}