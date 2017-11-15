package io.geeny.sdk.clients.custom

import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.common.ClientValueCache
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.SimpleCache
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.reactivex.Completable
import io.reactivex.Observable

class AppClient(
        private val address: String,
        private val slots: Map<String, Slot>) : Client {
    override fun geenyInformation(): Observable<LocalThingInfo> =
        Observable.just(LocalThingInfo(
                name(),
                address,
                1,
                address,
                address
        ))


    override fun name(): String = "Application Client"

    private val slotCache = object : SimpleCache<Slot>() {
        override fun id(value: Slot): String = value.id()
    }

    override fun getSlot(slotId: String): Observable<Slot> = slotCache.get(slotId)
    override fun slots(): Observable<List<Slot>> = slotCache.list()

    override fun slotState(slotId: String): Observable<ConnectionState> = (slots[slotId]!!).state()

    private val valueCache = ClientValueCache()

    init {
        // subscribe interceptors
        slots.forEach {
            // connectSecure resource to cache
            it.value.messagesToWrite().subscribe {
                valueCache.setValue(it)
            }
            // put resource to cache
            slotCache.save(it.value).subscribe()
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
        if (slots.containsKey(resourceId)) {
            slots[resourceId]!!.onNotificationEnabled(resourceId, enable)
            valueCache.enableNotification(resourceId, enable)
        }

        return Completable.complete()
    }

    private fun setValue(resourceId: String, bytes: ByteArray) {
        slots[resourceId]?.onMessageArrived(ClientMessage(resourceId, bytes))
    }

    override fun address(): String = address
    override fun connection(): Observable<ConnectionState> = Observable.just(ConnectionState.CONNECTED)
    override fun disconnect(): Completable = Completable.complete()
    override fun connect(): Completable = Completable.complete()

    companion object {
        val TAG = AppClient::class.java.simpleName
    }
}