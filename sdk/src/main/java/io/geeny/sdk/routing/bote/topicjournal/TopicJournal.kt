package io.geeny.sdk.routing.bote.topicjournal


interface TopicJournal {
    fun send(request: BoteRequest): BoteResponse
    fun info(): TopicInfo
    fun type(): TopicJournalType
}


enum class TopicJournalType {
    FAN_OUT,
    DISMISS_ON_READ,
    OVERWRITE
}


data class TopicInfo(val id: String, val topicJournalType: TopicJournalType, val maxCacheSize: Long = 1_000_000)

data class Iterator(val id: String)

fun createIterator(salt: Int): Iterator = Iterator("it_" + System.currentTimeMillis() + "$salt")


enum class MessageType {
    CAST,
    CALL,
    SUBSCRIBE,
    ACK,
    READ,
    EMPTY
}

data class BoteRequest(val messageType: MessageType, val payload: ByteArray?, val lastIterator: String?)

data class BoteResponse(val messageType: MessageType, val payload: ByteArray?, val lastIterator: String?)

fun emptyBoteResponse(): BoteResponse = BoteResponse(MessageType.EMPTY, null, null)
fun emptyBoteResponseWithIterator(iterator: String): BoteResponse = BoteResponse(MessageType.EMPTY, null, iterator)