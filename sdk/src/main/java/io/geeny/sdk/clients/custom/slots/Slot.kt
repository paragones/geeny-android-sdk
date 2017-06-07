package io.geeny.sdk.clients.custom.slots

import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable

enum class ResourceType {
    SINK,
    SOURCE,
    CHANNEL;
}


interface Slot {
    fun id(): String
    fun types(): List<ResourceAccessType>
    fun state(): Observable<ConnectionState>
    fun name(): String
    fun onNotificationEnabled(resourceId: String, enable: Boolean)
    fun onMessageArrived(clientMessage: ClientMessage)
    fun messagesToWrite(): Observable<ClientMessage>
    fun type(): ResourceType
}

enum class ResourceAccessType {
    WRITE,
    READ,
    NOTIFY;
}