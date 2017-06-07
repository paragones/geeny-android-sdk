package io.geeny.sdk.common

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable
import java.lang.Exception

interface KeyValueStore {
    fun get(key: String): Observable<String>
    fun save(key: String, value: String): Observable<String>
    fun delete(id: String): Observable<Boolean>
}

class MemoryKeyValueStore() : KeyValueStore {

    val map: MutableMap<String, String> = HashMap()

    override fun get(key: String): Observable<String> = Observable.create { subscriber ->
        if (map.containsKey(key)) {
            subscriber.onNext(map[key]!!)
        }

        subscriber.onComplete()
    }

    override fun save(key: String, value: String): Observable<String> = Observable.create { subscriber ->
        map[key] = value
        subscriber.onNext(value)
        subscriber.onComplete()
    }

    override fun delete(key: String): Observable<Boolean> = Observable.create { subscriber ->
        if (map.containsKey(key)) {
            map.remove(key)
        }
        subscriber.onNext(true)
        subscriber.onComplete()
    }

}

class DefaultKeyValueStore(val context: Context) : KeyValueStore {
    private val prefs: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_URI, Context.MODE_PRIVATE)

    fun dump(): String {
        GLog.d(TAG, "Printing the whole database")
        val map = prefs.all
        val sb = StringBuilder()
        map.forEach {
            sb.append("\n................")
                    .append("\n")
                    .append(it.key)
                    .append(" ---->\n")
                    .append(it.value as String)
        }
        return sb.toString()
    }

    override fun delete(id: String): Observable<Boolean> = Observable.create { subscriber ->
        subscriber.onNext(prefs.edit().remove(id).commit())
        subscriber.onComplete()
    }

    override fun get(key: String): Observable<String> = Observable.create { subscriber ->
        val result = prefs.getString(key, "")
        if (result.isNotEmpty()) {
            subscriber.onNext(result)
        }
        subscriber.onComplete()
    }

    override fun save(key: String, value: String): Observable<String> = Observable.create { subscriber ->
        val result = prefs.edit().putString(key, value).commit()
        if (result) {
            subscriber.onNext(value)
            subscriber.onComplete()
        } else {
            subscriber.onError(Exception("Couldn't save value with key: $key"))
        }
    }


    companion object {
        val SHARED_PREFS_URI = "io.geeny.key_value_store"
        val TAG = DefaultKeyValueStore::class.java.simpleName
    }
}