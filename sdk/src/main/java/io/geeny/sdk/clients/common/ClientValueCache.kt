package io.geeny.sdk.clients.common

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ClientValueCache {

    val valueStreams: MutableMap<String, PublishSubject<ByteArray>> = HashMap()
    val values: MutableMap<String, ByteArray> = HashMap()
    val notify: MutableMap<String, Boolean> = HashMap()

    fun read(resourceId: String): Observable<ByteArray> = Observable.create { subscriber ->
        if (values.containsKey(resourceId)) {
            val value = values[resourceId]!!
            subscriber.onNext(value)
            valueStreams[resourceId]!!.onNext(value)
        }
        subscriber.onComplete()
    }

    fun value(resourceId: String): Observable<ByteArray> {
        createValueStreamIfNecessary(resourceId)

        return if (notify[resourceId]!!) {
            Observable.merge(read(resourceId), valueStreams[resourceId]!!)
        } else {
            valueStreams[resourceId]!!
        }
    }

    fun createValueStreamIfNecessary(resourceId: String): Boolean {
        if (!valueStreams.containsKey(resourceId)) {
            valueStreams[resourceId] = PublishSubject.create()
            notify[resourceId] = false
            return true
        }

        return false
    }

    fun enableNotification(resourceId: String, enable: Boolean) {
        createValueStreamIfNecessary(resourceId)
        notify[resourceId] = enable
        if (enable && values.containsKey(resourceId)) {
            setValue(ClientMessage(resourceId, values[resourceId]!!))
        }
    }

    fun setValue(message: ClientMessage) {
        createValueStreamIfNecessary(message.resourceId)
        values[message.resourceId] = message.bytes
        if (notify[message.resourceId]!!) {
            valueStreams[message.resourceId]!!.onNext(message.bytes)
        }
    }
}