package io.geeny.sdk.common

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

abstract class SimpleCache<T> {
    private val map: MutableMap<String, T> = HashMap<String, T>()

    private val stream: BehaviorSubject<Map<String, T>> = BehaviorSubject.create()

    private fun toList() = map.toList().map { it.second }

    fun stream(): Observable<Map<String, T>> = stream

    fun get(id: String): Observable<T> = Observable.create { subscriber ->
        if (map.containsKey(id)) {
            subscriber.onNext(map[id]!!)
        }

        subscriber.onComplete()
    }

    fun save(value: T): Observable<T> = Observable.create { subscriber ->
        map.put(id(value), value)
        stream.onNext(map)
        subscriber.onNext(value)
        subscriber.onComplete()
    }


    fun save(value: List<T>): Observable<List<T>> = Observable.create { subscriber ->
        for (t in value) {
            map.put(id(t), t)
        }
        stream.onNext(map)
        subscriber.onNext(value)
        subscriber.onComplete()
    }

    fun list(): Observable<List<T>> = Observable.create { subscriber ->
        subscriber.onNext(toList())
        subscriber.onComplete()
    }

    fun clear(): Completable = Completable.create { subscriber ->
        map.clear()
        stream.onNext(map)
        subscriber.onComplete()
    }


    fun remove(id: String): Observable<T> = Observable.create{ subscriber ->
        if(map.containsKey(id)) {
            val value = map[id]!!
            map.remove(id)
            subscriber.onNext(value)
        }

        subscriber.onComplete()
    }

    abstract fun id(value: T): String
}