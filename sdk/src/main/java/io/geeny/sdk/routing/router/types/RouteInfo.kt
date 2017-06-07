package io.geeny.sdk.routing.router.types

import io.geeny.sdk.routing.router.Router

data class RouteInfo(
        val type: RouteType,
        val direction: Direction,
        val topic: String,
        val clientIdentifier: String,
        val clientResourceId: String) {
    fun identifier() = Router.identifier(clientIdentifier, type, direction, clientResourceId)
}

enum class RouteType {
    EMPTY,
    MQTT,
    BLE,
    CUSTOM
}

enum class Direction {
    CONSUMER,
    PRODUCER
}
