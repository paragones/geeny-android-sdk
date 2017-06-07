package io.geeny.sdk.clients.custom.slots.sink

import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.custom.slots.ResourceAccessType
import io.geeny.sdk.clients.custom.slots.ResourceType
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable

abstract class Sink(val name: String, val resourceId: String) : Slot {

    private val notificationStream: Stream<ConnectionState> = Stream()

    init {
        notificationStream.set(ConnectionState.DISCONNECTED)
    }

    override fun onNotificationEnabled(resourceId: String, enable: Boolean) {
        notificationStream.set(if (enable) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED)
    }

    override fun type(): ResourceType = ResourceType.SINK

    override fun id(): String = resourceId

    override fun types(): List<ResourceAccessType> = listOf(ResourceAccessType.READ, ResourceAccessType.NOTIFY)

    override fun state(): Observable<ConnectionState> = notificationStream.connect()

    override fun name(): String = name
}