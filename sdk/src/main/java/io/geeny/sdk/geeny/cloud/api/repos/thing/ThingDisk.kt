package io.geeny.sdk.geeny.cloud.api.repos.thing

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.geeny.cloud.api.repos.Thing
import io.geeny.sdk.geeny.cloud.api.repos.emptyCertificate
import org.json.JSONObject


object ThingJsonConverter : JSONConverter<Thing> {

    val JSON_KEY_ID = "JSON_KEY_ID"
    val JSON_KEY_NAME = "JSON_KEY_NAME"
    val JSON_KEY_SERIAL_NUMBER = "JSON_KEY_SERIAL_NUMBER"
    val JSON_KEY_THING_TYPE = "JSON_KEY_THING_TYPE"
    val JSON_KEY_CREATED = "JSON_KEY_CREATED"
    override fun id(value: Thing): String = value.serial_number

    override fun fromJSON(json: JSONObject): Thing =
            Thing(
                    json.getString(JSON_KEY_ID),
                    json.getString(JSON_KEY_NAME),
                    json.getString(JSON_KEY_SERIAL_NUMBER),
                    emptyCertificate,
                    json.getString(JSON_KEY_THING_TYPE),
                    json.getString(JSON_KEY_CREATED)
            )

    override fun toJSON(value: Thing): JSONObject =
            JSONObject().apply {
                put(JSON_KEY_ID, value.id)
                put(JSON_KEY_NAME, value.name)
                put(JSON_KEY_SERIAL_NUMBER, value.serial_number)
                put(JSON_KEY_THING_TYPE, value.thing_type)
                put(JSON_KEY_CREATED, value.created)
            }

}

