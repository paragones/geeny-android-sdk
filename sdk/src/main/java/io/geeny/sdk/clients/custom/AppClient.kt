package io.geeny.sdk.clients.custom

import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.common.ClientValueCache
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.SimpleCache
import io.reactivex.Completable
import io.reactivex.Observable

class AppClient(
        private val address: String,
        private val interceptors: Map<String, Slot>) : Client {
    // TODO refactor slot pool out of client and make a reasonable implementation of addResource and removeResource
    override fun addResource(slot: Slot): Observable<Slot> = Observable.just(slot)

    override fun removeResource(slot: Slot): Observable<Slot> = Observable.just(slot)

    private val resourceCache = object : SimpleCache<Slot>() {
        override fun id(value: Slot): String = value.id()
    }

    override fun getResource(id: String): Observable<Slot> = resourceCache.get(id)
    override fun resources(): Observable<List<Slot>> = resourceCache.list()

    override fun resourceState(resourceId: String): Observable<ConnectionState> = (interceptors[resourceId]!!).state()

    private val valueCache = ClientValueCache()

    init {
        // subscribe interceptors
        interceptors.forEach {
            // connectSecure resource to cache
            it.value.messagesToWrite().subscribe {
                valueCache.setValue(it)
            }
            // put resource to cache
            resourceCache.save(it.value).subscribe()
        }
    }

    override fun write(clientMessage: ClientMessage): Observable<ByteArray> = Observable.create<ByteArray> { subscriber ->
        setValue(clientMessage.resourceId, clientMessage.bytes)
        subscriber.onNext(clientMessage.bytes)
        subscriber.onComplete()
    }

    override fun read(resourceId: String): Observable<ByteArray> = valueCache.read(resourceId)
    override fun value(resourceId: String): Observable<ByteArray> = valueCache.value(resourceId)

    override fun notify(resourceId: String, enable: Boolean): Completable {
        GLog.d(TAG, "notify: $enable")

        if (interceptors.containsKey(resourceId)) {
            interceptors[resourceId]!!.onNotificationEnabled(resourceId, enable)
            valueCache.enableNotification(resourceId, enable)
        }

        return Completable.complete()
    }

    private fun setValue(resourceId: String, bytes: ByteArray) {
        if (interceptors.containsKey(resourceId)) {
            interceptors[resourceId]!!.onMessageArrived(ClientMessage(resourceId, bytes))
        }
    }

    override fun address(): String = address
    override fun connection(): Observable<ConnectionState> = Observable.just(ConnectionState.CONNECTED)
    override fun disconnect(): Completable = Completable.complete()
    override fun connect(): Completable = Completable.complete()

    companion object {
        val TAG = AppClient::class.java.simpleName
    }
}