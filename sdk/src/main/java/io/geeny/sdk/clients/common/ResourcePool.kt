package io.geeny.sdk.clients.common

import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.SimpleCache
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class ResourcePool {

    val stream: Stream<List<Slot>> = Stream()
    val disposables: MutableMap<String, Disposable> = HashMap()
    val messageStream: PublishSubject<ClientMessage> = PublishSubject.create()

    private val resourceCache = object : SimpleCache<Slot>() {
        override fun id(value: Slot): String = value.id()
    }

    init {
        stream.set(emptyList())
    }

    fun list(): Observable<List<Slot>> = resourceCache.list()
    fun stream(): Observable<List<Slot>> = stream.connect()

    fun state(id: String): Observable<ConnectionState> = get(id).flatMap { state(id) }
    fun get(id: String): Observable<Slot> = resourceCache.get(id)
    fun add(slot: Slot): Observable<Slot> = Observable.just(slot)
            .flatMap { resourceCache.save(it) }
            .flatMap { connectResource(it) }

    fun remove(id: String): Observable<Slot> = get(id)
            .flatMap { resourceCache.remove(it.id()) }
            .flatMap { disconnectResource(it) }

    private fun connectResource(slot: Slot): Observable<Slot> = Observable.create { subscriber ->
        disposables.put(slot.id(), slot.messagesToWrite().subscribe { messageStream })
        subscriber.onNext(slot)
        subscriber.onComplete()
    }

    private fun disconnectResource(slot: Slot): Observable<Slot> = Observable.create { subscriber ->
        disposables.remove(slot.id())
        subscriber.onNext(slot)
        subscriber.onComplete()
    }

    fun value(): Observable<ClientMessage> = messageStream
}