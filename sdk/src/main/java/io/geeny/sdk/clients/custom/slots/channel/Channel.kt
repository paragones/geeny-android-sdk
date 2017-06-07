package io.geeny.sdk.clients.custom.slots.channel

import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.custom.slots.ResourceType
import io.geeny.sdk.clients.custom.slots.ResourceAccessType
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class Channel(val resourceId: String) : Slot {
    private val notification: Stream<ConnectionState> = Stream()

    override fun types(): List<ResourceAccessType> = listOf(ResourceAccessType.READ, ResourceAccessType.NOTIFY, ResourceAccessType.WRITE)
    override fun state(): Observable<ConnectionState> = notification.connect()

    private var enabled = false
    override fun id() = resourceId
    private val messageSubject: PublishSubject<ClientMessage> = PublishSubject.create()
    override fun type(): ResourceType = ResourceType.CHANNEL

    init {
        notification.set(ConnectionState.DISCONNECTED)
    }

    override fun onNotificationEnabled(resourceId: String, enable: Boolean) {
        if (this.resourceId == resourceId) {
            enabled = enable
            notification.set(if (enabled) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED)
        }
    }

    override fun onMessageArrived(clientMessage: ClientMessage) {
        if (clientMessage.resourceId == resourceId) {
            flatMap(clientMessage.bytes)
                    .subscribe {
                        messageSubject.onNext(ClientMessage(resourceId, it))
                    }
        }
    }

    override fun messagesToWrite(): Observable<ClientMessage> = messageSubject

    abstract fun flatMap(array: ByteArray): Observable<ByteArray>
}