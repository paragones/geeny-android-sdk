package io.geeny.sdk.geeny.cloud.api.repos.resource

import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.cloud.api.endpoints.ResourceListResponse
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingTypeEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingTypeResponse
import io.geeny.sdk.geeny.cloud.api.endpoints.resourceResponse2Resource
import io.geeny.sdk.geeny.cloud.api.repos.Resource
import io.geeny.sdk.geeny.cloud.api.repos.SimpleCache
import io.geeny.sdk.geeny.cloud.api.repos.ThingType
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.json.JSONArray
import org.json.JSONObject

class ThingTypeRepository(
        private val endpoint: ThingTypeEndpoint,
        private val cache: SimpleCache<ThingType>,
        private val disk: ListDisk<ThingType>
) {

    fun getOrDownload(id: String): Observable<ThingType> =
            get(id)
                    .switchIfEmpty(download(id))

    fun get(id: String): Observable<ThingType> =
            cache.get(id)
                    .switchIfEmpty(disk.get(id).flatMap { cache.save(it) })

    fun download(id: String): Observable<ThingType> =
            endpoint.get(id)
                    .doOnNext { GLog.d(TAG, "Need to download thingtype $id") }
                    .flatMap {
                        Observable.zip<ThingTypeResponse, ResourceListResponse, ThingType>(
                                Observable.just(it),
                                endpoint.listResources(id),
                                BiFunction { (id1, name, created), resList ->
                                    val tt =
                                    ThingType(
                                            id1,
                                            name,
                                            created,
                                            resList.data.map { resourceResponse2Resource(it) }
                                    )

                                    GLog.d(TAG, "Downloaded ThingType $tt")
                                    tt
                                }
                        )
                    }.flatMap { save(it) }


    fun save(thingType: ThingType) =
            disk.save(thingType).flatMap { cache.save(it) }


    companion object {
        val TAG = ThingTypeRepository::class.java.simpleName
    }
}

object ThingTypeJsonConverter : JSONConverter<ThingType> {
    private val JSON_KEY_ID = "JSON_KEY_ID"
    private val JSON_KEY_NAME = "JSON_KEY_NAME"
    private val JSON_KEY_CREATED = "JSON_KEY_CREATED"
    private val JSON_KEY_RESOURCES = "JSON_KEY_RESOURCES"


    override fun toJSON(value: ThingType): JSONObject {
        val json = JSONObject()

        json.put(JSON_KEY_ID, value.id)
        json.put(JSON_KEY_NAME, value.name)
        json.put(JSON_KEY_CREATED, value.created)

        val resourcesArray = JSONArray()

        for (resource in value.resources) {
            resourcesArray.put(ResourceJsonConverter.toJSON(resource))
        }

        json.put(JSON_KEY_RESOURCES, resourcesArray)

        return json
    }

    override fun fromJSON(json: JSONObject): ThingType {

        val id = json.getString(JSON_KEY_ID)
        val name = json.getString(JSON_KEY_NAME)
        val created = json.getString(JSON_KEY_NAME)


        val resourcesArray = json.getJSONArray(JSON_KEY_RESOURCES)

        val resources: MutableList<Resource> = ArrayList()

        (0 until resourcesArray.length()).mapTo(resources) {
            ResourceJsonConverter.fromJSON(resourcesArray.getJSONObject(it))
        }


        return ThingType(
                id,
                name,
                created,
                resources
        )
    }

    override fun id(value: ThingType): String = value.id

}


object ResourceJsonConverter : JSONConverter<Resource> {
    private val JSON_KEY_URI = "JSON_KEY_URI"
    private val JSON_KEY_METHOD = "JSON_KEY_METHOD"
    private val JSON_KEY_MESSAGE_TYPE = "JSON_KEY_MESSAGE_TYPE"

    override fun id(value: Resource): String = value.uri

    override fun toJSON(value: Resource): JSONObject {
        val json = JSONObject()

        json.put(JSON_KEY_URI, value.uri)
        json.put(JSON_KEY_METHOD, value.method)
        json.put(JSON_KEY_MESSAGE_TYPE, value.messageType)

        return json
    }

    override fun fromJSON(json: JSONObject): Resource =
            Resource(
                    json.getString(JSON_KEY_URI),
                    json.getString(JSON_KEY_METHOD),
                    json.getString(JSON_KEY_MESSAGE_TYPE)
            )
}

