package io.geeny.sdk.clients.common

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class Stream<T> {

    var value: T? = null
    private val subject: PublishSubject<T> = PublishSubject.create()

    fun connect(): Observable<T> = if (value == null) subject else Observable.merge(Observable.just(value!!), subject)

    fun set(value: T) {
        this.value = value
        subject.onNext(this.value)
    }

    fun hasValue() = value != null
}