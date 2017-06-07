package io.geeny.sdk.common

import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject

open class ListDisk<T>(private val store: KeyValueStore, private val converter: JSONConverter<T>, private val listId: String) {

    fun save(value: T): Observable<T> =
            Observable.just(value)
                    .map { converter.toJSON(it) }
                    .map { it.toString() }
                    .flatMap { store.save(id(value), it) }
                    .map { JSONObject(it) }
                    .map { converter.fromJSON(it) }
                    .flatMap {
                        addToList(it)
                    }

    fun remove(value: T): Observable<T> =
            store.delete(id(value))
                    .flatMap { removeFromList(id(value)) }
                    .map { value }

    private fun addToList(value: T): Observable<T> =
            store.get(listId)
                    .map { JSONArray(it) }
                    .defaultIfEmpty(JSONArray())
                    .map {
                        it.put(id(value))
                        it
                    }
                    .map { it.toString() }
                    .flatMap { store.save(listId, it) }
                    .map { value }

    private fun removeFromList(id: String): Observable<String> =
            store.get(listId)
                    .map { JSONArray(it) }
                    .map { arrayToList(it) }
                    .map {
                        val set = it.toMutableSet()
                        set.remove(id)
                        set.toList()
                    }
                    .map { listToArray(it) }
                    .map { it.toString() }
                    .flatMap { store.save(listId, it) }
                    .map { id }

    fun get(id: String): Observable<T> =
            store.get(id(id))
                    .map { JSONObject(it) }
                    .map { converter.fromJSON(it) }


    private fun getFromList(id: String): Observable<T> =
            store.get(id)
                    .map { JSONObject(it) }
                    .map { converter.fromJSON(it) }

    fun list(): Observable<List<T>> =
            store.get(listId)
                    .map { JSONArray(it) }
                    .map { arrayToList(it) }
                    .flatMapIterable { it }
                    .flatMap { getFromList(it) }
                    .toList()
                    .toObservable()


    fun save(list: List<T>): Observable<List<T>> =
            Observable.just(list)
                    .flatMapIterable { it }
                    .map { Pair(id(it), converter.toJSON(it).toString()) }
                    .flatMap { store.save(it.first, it.second) }
                    .map { converter.fromJSON(JSONObject(it)) }
                    .toList()
                    .toObservable()

    fun id(value: T): String = id(converter.id(value))
    fun id(value: String): String = listId + "_" + value

    companion object {

        private fun arrayToList(array: JSONArray): List<String> {
            val list: MutableList<String> = ArrayList()
            (0 until array.length()).mapTo(list) { array.getString(it) }
            return list
        }

        private fun listToArray(list: List<String>): JSONArray {
            val array = JSONArray()
            list.forEach { array.put(it) }
            return array
        }

    }
}