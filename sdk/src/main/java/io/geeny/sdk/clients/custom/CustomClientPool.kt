package io.geeny.sdk.clients.custom

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class CustomClientPool(val configuration: GeenyConfiguration) {
    private val map: MutableMap<String, AppClient> = HashMap()
    private fun toList(): List<AppClient> = map.toList().map { it.second }
    private val availableClientsStream: PublishSubject<List<AppClient>> = PublishSubject.create()

    fun availableClients(): Observable<List<AppClient>> = Observable.merge(Observable.just(toList()), availableClientsStream)

    init {
        val appClient = AppClient(APP_CLIENT_ADDRESS, configuration.interceptors)
        map.put(APP_CLIENT_ADDRESS, appClient)
    }

    private fun tearDown() {
        toList().forEach { it.disconnect() }
    }

    fun get(address: String): Observable<AppClient> = Observable.create { subscriber ->
        if (map.containsKey(address)) {
            subscriber.onNext(map[address]!!)
        }
        subscriber.onComplete()
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap {
                    tearDown()
                    Observable.just(it)
                }
    }

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
    }

    companion object {
        val TAG = CustomClientPool::class.java.simpleName
        val APP_CLIENT_ADDRESS = "application.client.identifier"
    }
}