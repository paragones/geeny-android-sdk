package io.geeny.sdk.clients.custom.slots.channel

import io.reactivex.Observable

abstract class MapChannel(resourceId: String) : Channel(resourceId) {
    override fun flatMap(array: ByteArray): Observable<ByteArray> = Observable.create { subscriber ->
        subscriber.onNext(map(array))
        subscriber.onComplete()
    }

    abstract fun map(array: ByteArray): ByteArray
}