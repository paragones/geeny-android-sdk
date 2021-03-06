package io.geeny.sdk.common.environment

import android.content.Intent
import android.net.Uri

interface Environment {
    fun thingApiBaseUrl(): String

    fun type(): Type

    fun signInIntent(): Intent

    fun geenyConnectBaseUrl(): String

    fun geenyMqttBrokerUrl(): String

    enum class Type {
        PRODUCTION,
        DEVELOPMENT
    }
}

class ProductionEnvironment : Environment {
    override fun geenyMqttBrokerUrl(): String = "ssl://mqtt.geeny.io:8883"

    override fun geenyConnectBaseUrl() = "https://connect.geeny.io/auth/login/"

    override fun thingApiBaseUrl(): String = "https://labs.geeny.io/things/api/v1/"

    override fun type(): Environment.Type = Environment.Type.PRODUCTION

    override fun signInIntent(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(signInIntentUrl))

    companion object {
        val signInIntentUrl = "https://labs.geeny.io/login?next=/oauth2-provider/authorize/%3Fresponse_type%3Dcode%26redirect_uri%3Dhttps%253A%252F%252Fhomesmarthome.geeny.io%252Fauth%252Fredirect%26client_id%3D4FZGivo0d234wvneFFxXTaaK0cbHukJifJNrg0Jr"
    }
}

class DevelopmentEnvironment: Environment {
    override fun geenyMqttBrokerUrl(): String =  "ssl://batcave.geeny.io:8883"

    override fun geenyConnectBaseUrl() = "https://batcave-connect.geeny.io/auth/login/"

    override fun thingApiBaseUrl(): String = "https://batcave.geeny.io/things/api/v1/"

    override fun type(): Environment.Type = Environment.Type.DEVELOPMENT

    override fun signInIntent(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(signInIntentUrl))

    companion object {
        val signInIntentUrl = "https://labs.geeny.io/login?next=/oauth2-provider/authorize/%3Fresponse_type%3Dcode%26redirect_uri%3Dhttps%253A%252F%252Fhomesmarthome.geeny.io%252Fauth%252Fredirect%26client_id%3D4FZGivo0d234wvneFFxXTaaK0cbHukJifJNrg0Jr"
    }
}