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

class VirtualThing(
        private val name: String,
        private val thingTypeId: String,
        private val id: String,
        private val slots: Map<String, Slot>) : Client {
    override fun geenyInformation(): Observable<LocalThingInfo> =
            Observable.just(LocalThingInfo(
                    name(),
                    address(),
                    1,
                    id,
                    thingTypeId
            ))

    override fun name(): String = name

    private val slotCache = object : SimpleCache<Slot>() {
        override fun id(value: Slot): String = value.id()
    }

    override fun getSlot(slotId: String): Observable<Slot> = slotCache.get(slotId)
    override fun slots(): Observable<List<Slot>> = slotCache.list()
    override fun slotState(slotId: String): Observable<ConnectionState> = (slots[slotId]!!).state()

    private val valueCache = ClientValueCache()

    init {
        // subscribe slots
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
        GLog.i(TAG, "Writing to $clientMessage")
        setValue(clientMessage.resourceId, clientMessage.bytes)
        subscriber.onNext(clientMessage.bytes)
        subscriber.onComplete()
    }

    override fun read(slotId: String): Observable<ByteArray> = valueCache.read(slotId)
    override fun value(slotId: String): Observable<ByteArray> = valueCache.value(slotId)

    override fun notify(slotId: String, enable: Boolean): Completable {
        if (enable) {
            GLog.i(TAG, "starting slot: $slotId")
        } else {
            GLog.i(TAG, "stopping slot: $slotId")
        }

        if (slots.containsKey(slotId)) {
            slots[slotId]?.onNotificationEnabled(slotId, enable)
            valueCache.enableNotification(slotId, enable)
        }

        return Completable.complete()
    }

    private fun setValue(resourceId: String, bytes: ByteArray) {
        slots[resourceId]?.onMessageArrived(ClientMessage(resourceId, bytes))
    }

    override fun address(): String = thingTypeId
    override fun connection(): Observable<ConnectionState> = Observable.just(ConnectionState.CONNECTED)
    override fun disconnect(): Completable = Completable.complete()
    override fun connect(): Completable = Completable.complete()

    companion object {
        val TAG = VirtualThing::class.java.simpleName
    }

    class Builder(val name: String, val thingTypeId: String, val id: String) {
        val slots: MutableList<Slot> = ArrayList()

        fun withSlot(slot: Slot): Builder {
            slots.add(slot)
            return this
        }

        private fun slotsToMap(s: List<Slot>): Map<String, Slot> {
            val map: MutableMap<String, Slot> = HashMap()

            s.forEach {
                map.put(it.id(), it)
            }

            return map
        }

        fun build(): VirtualThing =
                VirtualThing(name, thingTypeId, id, slotsToMap(slots))
    }
}