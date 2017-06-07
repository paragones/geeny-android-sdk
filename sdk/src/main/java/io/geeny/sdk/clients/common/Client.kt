package io.geeny.sdk.clients.common

import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Completable
import io.reactivex.Observable

interface Client {
    fun address(): String
    fun connection(): Observable<ConnectionState>
    fun disconnect(): Completable
    fun connect(): Completable
    fun write(clientMessage: ClientMessage): Observable<ByteArray>
    fun read(resourceId: String): Observable<ByteArray>
    fun value(resourceId: String): Observable<ByteArray>
    fun notify(resourceId: String, enable: Boolean): Completable
    fun resources(): Observable<List<Slot>>
    fun resourceState(resourceId: String): Observable<ConnectionState>
    fun getResource(id: String): Observable<Slot>
    fun addResource(slot: Slot): Observable<Slot>
    fun removeResource(slot: Slot): Observable<Slot>
}