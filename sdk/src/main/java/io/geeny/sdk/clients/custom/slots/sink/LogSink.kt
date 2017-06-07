package io.geeny.sdk.clients.custom.slots.sink

import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.toHex
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LogSink(name: String, resourceId: String, val tag: String) : Sink(name, resourceId) {


    private val messageSubject: PublishSubject<ClientMessage> = PublishSubject.create()

    override fun messagesToWrite(): Observable<ClientMessage> = messageSubject

    override fun onMessageArrived(clientMessage: ClientMessage) {
        val resourceId = clientMessage.resourceId
        GLog.d(tag, "resourceId: ${resourceId} - message: ${clientMessage.bytes.toHex()}")

        messageSubject.onNext(clientMessage)
    }
}