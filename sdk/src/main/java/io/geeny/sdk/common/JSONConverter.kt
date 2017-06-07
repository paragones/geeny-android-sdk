package io.geeny.sdk.common

import org.json.JSONObject

interface JSONConverter<T> {
    fun id(value: T): String
    fun toJSON(value: T): JSONObject
    fun fromJSON(json: JSONObject): T
}