package io.geeny.sdk.routing.bote


import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.TypeConverters
import io.geeny.sdk.common.toHex
import io.geeny.sdk.routing.bote.topicjournal.*
import io.reactivex.Observable


open class BoteBroker(keyValueStore: KeyValueStore) {

    private val topics: MutableMap<String, TopicJournal> = mutableMapOf()
    private val topicStream: Stream<List<TopicInfo>> = Stream()

    private val topicDisk: TopicInfoDisk = TopicInfoDisk(keyValueStore)

    fun createTopic(topic: String, type: TopicJournalType = TopicJournalType.DISMISS_ON_READ): Observable<TopicInfo> = Observable.create<TopicInfo> { subscriber ->
        val info = TopicInfo(topic, type)
        topics.put(topic, createJournal(info))
        topicStream.set(topicsJournalMapToTopicInfoList())
        subscriber.onNext(info)
        subscriber.onComplete()
    }.flatMap { topicDisk.save(it) }


    private fun createJournal(info: TopicInfo): TopicJournal = when (info.topicJournalType) {
        TopicJournalType.FAN_OUT -> FanOutTopicJournal(info)
        TopicJournalType.DISMISS_ON_READ -> DismissOnReadTopicJournal(info)
        TopicJournalType.OVERWRITE -> OverwriteTopicJournal(info)
    }

    fun removeTopic(topic: String): Observable<String> = Observable.create<String> { subscriber ->
        if (topics.containsKey(topic)) {
            topics.remove(topic)
        }

        topicStream.set(topicsJournalMapToTopicInfoList())
        subscriber.onNext(topic)
        subscriber.onComplete()
    }.flatMap {
        // type is not necessary
        topicDisk.remove(TopicInfo(it, TopicJournalType.DISMISS_ON_READ)).map { it.id }
    }

    fun send(topic: String, payload: ByteArray): BoteResponse {
        if (topics.containsKey(topic)) {
            val value = TypeConverters.bytesToIntDynamic(payload)
            GLog.d(TAG, "Sending $topic $value")
            val ret = topics[topic]!!.send(BoteRequest(MessageType.CAST, payload, null))
            GLog.d(TAG, "Sent $topic")
            return ret
        }

        return emptyBoteResponse()
    }

    fun read(topic: String, iterator: String? = null): BoteResponse {

        if (topics.containsKey(topic)) {
            GLog.d(TAG, "Reading $topic")
            val ret = topics[topic]!!.send(BoteRequest(MessageType.READ, payload = null, lastIterator = iterator))
            val value = if (ret.payload != null) TypeConverters.bytesToIntDynamic(ret.payload!!) else -1
            GLog.d(TAG, "Read ${value}")
            return ret
        }

        return emptyBoteResponse()
    }

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .flatMap { restore(it) }
                .defaultIfEmpty(result)
    }

    fun restore(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return topicDisk.list()
                .flatMapIterable { it }
                .map {
                    val info = it
                    topics[info.id] = createJournal(info)
                    topicStream.set(topicsJournalMapToTopicInfoList())
                }
                .toList().toObservable()
                .map {
                    result.copy(topicsLoaded = it.size)
                    result
                }


    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
    }

    fun list(): Observable<List<TopicInfo>> = Observable.just(topics).map { it.toList().map { it.second.info() } }

    private fun topicsJournalMapToTopicInfoList() = topics.toList().map { it.second.info() }

    companion object {
        val TAG = BoteBroker::class.java.simpleName
    }
}

