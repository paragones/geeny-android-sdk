package io.geeny.sdk.geeny.auth

import org.json.JSONObject


fun emptyToken() = AuthToken("")

data class AuthToken(val token: String) {
    fun isEmpty(): Boolean = token.isEmpty()
}

data class Credentials(val email: String, val password: String)

fun Credentials.isValid(): Boolean {
    return email.isNotEmpty() && password.isNotEmpty()
}


fun Credentials.toJSON(): JSONObject {
    val json = JSONObject()

    json.put("email", email)
    json.put("password", password)

    return json
}