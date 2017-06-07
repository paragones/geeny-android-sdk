package io.geeny.sdk.routing.bote

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import org.json.JSONObject

private val TOPIC_INFO_LIST_ID = "TOPIC_INFO_LIST_ID"

class TopicInfoDisk(keyValueStore: KeyValueStore) : ListDisk<TopicInfo>(keyValueStore, TopicInfoConverter(), TOPIC_INFO_LIST_ID)

class TopicInfoConverter : JSONConverter<TopicInfo> {
    private val JSON_KEY_ID = "JSON_KEY_ID"
    private val JSON_KEY_TOPIC_JOURNAL_TYPE = "JSON_KEY_TOPIC_JOURNAL_TYPE"
    private val JSON_KEY_MAX_CACHE_SIZE = "JSON_KEY_MAX_CACHE_SIZE"

    override fun id(value: TopicInfo) = value.id

    override fun toJSON(value: TopicInfo): JSONObject =
            JSONObject().apply {
                put(JSON_KEY_ID, value.id)
                put(JSON_KEY_TOPIC_JOURNAL_TYPE, value.topicJournalType)
                put(JSON_KEY_MAX_CACHE_SIZE, value.maxCacheSize)
            }

    override fun fromJSON(json: JSONObject): TopicInfo =
            TopicInfo(
                    json.getString(JSON_KEY_ID),
                    TopicJournalType.valueOf(json.getString(JSON_KEY_TOPIC_JOURNAL_TYPE)),
                    json.getLong(JSON_KEY_MAX_CACHE_SIZE)
            )

}