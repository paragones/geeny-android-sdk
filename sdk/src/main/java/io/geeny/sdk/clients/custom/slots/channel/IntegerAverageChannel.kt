package io.geeny.sdk.clients.custom.slots.channel

import io.geeny.sdk.common.TypeConverters
import io.reactivex.Observable

class IntegerAverageChannel(val name: String, resourceId: String, val averageOver: Int) : Channel(resourceId) {
    override fun name(): String = name

    private val bucket: MutableList<Int> = ArrayList()

    override fun flatMap(array: ByteArray): Observable<ByteArray> {
        val original = TypeConverters.bytesToInt(array)
        bucket.add(original)

        return if (bucket.size == averageOver) {
            val sum = bucket.sum() / averageOver
            bucket.clear()
            Observable.just(TypeConverters.intToBytes(sum))
        } else {
            Observable.empty<ByteArray>()
        }
    }
}