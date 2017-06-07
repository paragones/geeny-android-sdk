package io.geeny.sdk.routing.router.types

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import org.json.JSONObject

val ROUTER_LIST_ID = "ROUTER_LIST_ID"

class RouterDisk(keyValueStore: KeyValueStore) : ListDisk<RouteInfo>(keyValueStore, RouteInfoConvert(), ROUTER_LIST_ID)

class RouteInfoConvert : JSONConverter<RouteInfo> {

    private val TYPE = "type"
    private val DIRECTION = "direction"
    private val TOPIC = "topic"
    private val CLIENT_IDENTIFIER = "clientIdentifier"
    private val CLIENT_RESOURCE_ID = "clientResourceId"

    override fun id(value: RouteInfo): String = value.identifier()
    override fun toJSON(value: RouteInfo): JSONObject =
            JSONObject().apply {
                put(TYPE, value.type.name)
                put(DIRECTION, value.direction)
                put(CLIENT_IDENTIFIER, value.clientIdentifier)
                put(CLIENT_RESOURCE_ID, value.clientResourceId)
                put(TOPIC, value.topic)
            }

    override fun fromJSON(json: JSONObject): RouteInfo =
            RouteInfo(
                    RouteType.valueOf(json.getString(TYPE)),
                    Direction.valueOf(json.getString(DIRECTION)),
                    json.getString(TOPIC),
                    json.getString(CLIENT_IDENTIFIER),
                    json.getString(CLIENT_RESOURCE_ID))

}