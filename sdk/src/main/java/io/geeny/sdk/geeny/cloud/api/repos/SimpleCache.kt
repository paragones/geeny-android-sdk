package io.geeny.sdk.geeny.cloud.api.repos

import io.reactivex.Observable


abstract class SimpleCache<T>(val map: MutableMap<String, T> = HashMap()) {

    fun get(id: String): Observable<T> {
        return Observable.create {
            subscriber ->
            if (map.containsKey(id)) {
                subscriber.onNext(map[id]!!)
            }
            subscriber.onComplete()
        }
    }

    fun list() :Observable<List<T>> {
        return Observable.create {
            subscriber ->
            subscriber.onNext(map.values.toList())
            subscriber.onComplete()
        }
    }

    fun save(t: T): Observable<T> {
        return Observable.create {
            subscriber ->
            map.put(id(t), t)
            subscriber.onNext(t)
            subscriber.onComplete()
        }
    }

    fun save(list: List<T>): Observable<List<T>> {
        return Observable.create {
            subscriber ->
            for (t in list) {
                map.put(id(t), t)
            }
            subscriber.onNext(list)
            subscriber.onComplete()
        }
    }

    abstract fun id(t: T): String
}