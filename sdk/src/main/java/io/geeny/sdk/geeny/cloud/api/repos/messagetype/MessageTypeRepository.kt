package io.geeny.sdk.geeny.cloud.api.repos.messagetype

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.cloud.api.endpoints.MessageTypeEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.messageTypeResponse2MessageType
import io.geeny.sdk.geeny.cloud.api.repos.MessageType
import io.geeny.sdk.geeny.cloud.api.repos.SimpleCache
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject

class MessageTypeRepository(
        val endpoint: MessageTypeEndpoint,
        val cache: SimpleCache<MessageType>,
        val disk: ListDisk<MessageType>
) {

    fun get(id: String): Observable<MessageType> =
            cache.get(id)
                    .switchIfEmpty(disk.get(id).flatMap { cache.save(it) })
                    .switchIfEmpty(download(id))

    fun save(messageType: MessageType): Observable<MessageType> =
            disk.save(messageType)
                    .flatMap { cache.save(it) }

    fun download(id: String): Observable<MessageType> {
        return endpoint.get(id).map {
            messageTypeResponse2MessageType(it)
        }.flatMap { save(it) }
    }

    fun list(): Observable<List<MessageType>> {
        return endpoint.list().map {
            it.data.map {
                messageTypeResponse2MessageType(it)
            }
        }
    }

    fun list(offset: Int, limit: Int): Observable<List<MessageType>> {
        return endpoint.list(offset, limit).map {
            it.data.map {
                messageTypeResponse2MessageType(it)
            }
        }
    }
}

object MessageTypeJsonConverter : JSONConverter<MessageType> {
    private val JSON_KEY_ID = "JSON_KEY_ID"
    private val JSON_KEY_NAME = "JSON_KEY_NAME"
    private val JSON_KEY_DESCRIPTION = "JSON_KEY_DESCRIPTION"
    private val JSON_KEY_MEDIA_TYPE = "JSON_KEY_MEDIA_TYPE"
    private val JSON_KEY_CREATED = "JSON_KEY_CREATED"
    private val JSON_KEY_TAGS = "JSON_KEY_TAGS"

    override fun id(value: MessageType): String = value.id

    override fun toJSON(value: MessageType): JSONObject {
        val json = JSONObject()

        json.put(JSON_KEY_ID, value.id)
        json.put(JSON_KEY_NAME, value.name)
        json.put(JSON_KEY_DESCRIPTION, value.description)
        json.put(JSON_KEY_MEDIA_TYPE, value.mediaType)
        json.put(JSON_KEY_CREATED, value.created)

        val tagsArray = JSONArray()

        for (tag in value.tags) {
            tagsArray.put(tag)
        }

        json.put(JSON_KEY_TAGS, tagsArray)
        return json
    }

    override fun fromJSON(json: JSONObject): MessageType {
        val tagsArray = json.getJSONArray(JSON_KEY_TAGS)
        val tags: MutableList<String> = ArrayList()
        (0 until tagsArray.length()).mapTo(tags) { tagsArray.getString(it) }

        return MessageType(
                json.getString(JSON_KEY_ID),
                json.getString(JSON_KEY_NAME),
                json.getString(JSON_KEY_DESCRIPTION),
                json.getString(JSON_KEY_MEDIA_TYPE),
                json.getString(JSON_KEY_CREATED),
                tags

        )
    }

}