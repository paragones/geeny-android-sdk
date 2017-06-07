package io.geeny.sdk.routing.bote.topicjournal

import io.geeny.sdk.common.GLog
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class FanOutTopicJournal(val info: TopicInfo, val maxCacheSize: Long = 1_000_000_000L): TopicJournal {
    override fun info(): TopicInfo = info
    override fun type(): TopicJournalType = TopicJournalType.FAN_OUT

    private var cacheSize: Long = 0
    private val cache: MutableMap<String, ByteArray> = mutableMapOf()
    private val nextIteratorMap: MutableMap<String, Iterator> = mutableMapOf()
    private var currentIterator: Iterator? = null
    private var firstIterator: Iterator? = null

    private val mutex: Lock = ReentrantLock()

    private val rand = Random()

    override fun send(request: BoteRequest): BoteResponse {
        when (request.messageType) {
            MessageType.CAST -> return cast(request)
            MessageType.CALL -> TODO()
            MessageType.SUBSCRIBE -> TODO()
            MessageType.ACK -> TODO()
            MessageType.READ -> return read(request)
        }

        return emptyBoteResponse()
    }

    private fun read(request: BoteRequest): BoteResponse {
        var response: BoteResponse = emptyBoteResponse()
        mutex.lock()
        GLog.i(TAG,"read $request")

        var iterator = request.lastIterator

        // no iterator in request object provided
        if (iterator == null) {
            // try currentIterator and return its cached value, otherwise return null
            if (currentIterator != null) {
                iterator = currentIterator!!.id
                val payload = cache[iterator]
                response = BoteResponse(MessageType.READ, payload, iterator)
            }
        } else { // iterator provided read next
            val newIterator = nextIteratorMap[iterator]

            // there is a valid next iterator, use it to return its cached payload
            if (newIterator != null) {
                val payload = cache[newIterator.id]
                response = BoteResponse(MessageType.READ, payload, newIterator.id)
            }
        }

        mutex.unlock()

        return response
    }


    private fun cast(request: BoteRequest): BoteResponse {
        mutex.lock()

        GLog.i(TAG,"cast $request")
        // create new iterator
        val iterator = createIterator(rand.nextInt())

        // point last iterator to newly created iterator
        if (currentIterator != null) {
            nextIteratorMap.put(currentIterator!!.id, iterator)
        } else {
            // it is first iterator
            firstIterator = iterator
        }

        // current iterator is newly created iterator
        currentIterator = iterator

        // store value
        cache.put(currentIterator!!.id, request.payload!!)

        val l = request.payload.size.toLong()
        cacheSize += l

        // cache size exceeded?
        if (cacheSize > maxCacheSize) {
            cleanUpCache()
        }

        mutex.unlock()

        return emptyBoteResponse()
    }

    fun cleanUpCache() {
        val removeSize = cacheSize - cacheSize / 4

        while (cacheSize > removeSize) {
            val itId = firstIterator!!.id

            val payload = cache[itId]
            cacheSize -= payload!!.size
            cache.remove(itId)
            firstIterator = nextIteratorMap[itId]
            nextIteratorMap.remove(itId)
        }
    }


    companion object {
        val TAG = FanOutTopicJournal::class.java.simpleName
    }
}