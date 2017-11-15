package io.geeny.sdk.clients.custom

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.common.Client
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class CustomClientPool(val configuration: GeenyConfiguration) {
    private val map: MutableMap<String, Client> = HashMap()
    private fun toList(): List<Client> = map.toList().map { it.second }
    private val availableClientsStream: PublishSubject<List<AppClient>> = PublishSubject.create()

    fun availableClients(): Observable<List<Client>> = Observable.merge(Observable.just(toList()), availableClientsStream)

    init {
        val appClient = AppClient(APP_CLIENT_ADDRESS, configuration.slots)
        map.put(APP_CLIENT_ADDRESS, appClient)
        configuration.virtualThings.forEach {
            map.put(it.address(), it)
        }
    }

    private fun tearDown() {
        toList().forEach { it.disconnect() }
    }

    fun get(address: String): Observable<Client> = Observable.create { subscriber ->
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