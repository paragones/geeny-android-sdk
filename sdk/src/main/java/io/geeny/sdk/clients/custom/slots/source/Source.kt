package io.geeny.sdk.clients.custom.slots.source

import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.custom.slots.ResourceType
import io.geeny.sdk.clients.custom.slots.ResourceAccessType
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


abstract class Source(val name: String, val resourceId: String, private val executorService: ExecutorService = Executors.newSingleThreadExecutor()) : Slot {

    override fun name() = name
    override fun id(): String = resourceId
    override fun types(): List<ResourceAccessType> = listOf(ResourceAccessType.NOTIFY, ResourceAccessType.READ)

    override fun type(): ResourceType = ResourceType.SOURCE

    private val messageStream: PublishSubject<ClientMessage> = PublishSubject.create()
    protected var isEnabled = false
    private val enabledStream: PublishSubject<ConnectionState> = PublishSubject.create()

    private fun connectionState(): ConnectionState = if (isEnabled) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED

    override fun state(): Observable<ConnectionState> = Observable.merge(Observable.just(connectionState()), enabledStream)

    override fun onNotificationEnabled(resourceId: String, enable: Boolean) {
        if (resourceId == this.resourceId) {
            isEnabled = enable
            enabledStream.onNext(connectionState())
            if (enable) {
                onEnabled()
            } else {
                onDisabled()
            }
        }
    }

    abstract fun onEnabled()

    abstract fun onDisabled()

    override fun onMessageArrived(clientMessage: ClientMessage) {
        // do nothing
    }

    override fun messagesToWrite(): Observable<ClientMessage> = messageStream

    fun notify(value: ByteArray) {
        messageStream.onNext(ClientMessage(resourceId, value))
    }
}