package io.geeny.sdk.clients.custom

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.reactivex.Observable

class CustomClientComponent(val configuration: GeenyConfiguration) {

    val pool: CustomClientPool by lazy {
        CustomClientPool(configuration)
    }

    fun getClient(address: String): Observable<AppClient> = pool.get(address)

    fun availableDevices(): Observable<List<AppClient>> = pool.availableClients()

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap { pool.onTearDown(it) }
    }

    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .flatMap { pool.onInit(it) }
    }
}