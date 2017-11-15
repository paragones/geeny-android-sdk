package io.geeny.sdk.clients.common

import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.reactivex.Completable
import io.reactivex.Observable

interface Client {
    fun name(): String
    fun address(): String
    fun connection(): Observable<ConnectionState>
    fun disconnect(): Completable
    fun connect(): Completable
    fun write(clientMessage: ClientMessage): Observable<ByteArray>
    fun read(slotId: String): Observable<ByteArray>
    fun value(slotId: String): Observable<ByteArray>
    fun notify(slotId: String, enable: Boolean): Completable
    fun slots(): Observable<List<Slot>>
    fun slotState(slotId: String): Observable<ConnectionState>
    fun getSlot(slotId: String): Observable<Slot>
    fun geenyInformation(): Observable<LocalThingInfo>
}