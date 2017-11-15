package io.geeny.sdk.clients.ble

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.geeny.DeviceInfoCache
import io.geeny.sdk.geeny.DeviceInfoDisk
import io.geeny.sdk.geeny.LocalThingInfoRepository
import io.reactivex.Observable

class BleComponent(
        private val configuration: GeenyConfiguration,
        private val keyValueStore: KeyValueStore,
        private val context: Context) {

    val pool: BleClientPool by lazy {
        BleClientPool(context, bleScanner)
    }

    private val bleScanner: BleScanner by lazy {
        BleScanner(context, localThingInfoRespository)
    }

    fun tearDown() {
        bleScanner.stopScan()
        pool.tearDown()
    }

    fun startScan() {
        bleScanner.startScan()
    }

    fun stopScan() {
        bleScanner.stopScan()
    }


    val localThingInfoRespository: LocalThingInfoRepository by lazy {
        LocalThingInfoRepository(DeviceInfoCache(), DeviceInfoDisk(keyValueStore))
    }

    fun getClient(address: String): Observable<BleClient> = pool.getOrCreate(address)
    fun availableDevices(): Observable<List<BleClient>> = pool.availableDevices()

    fun onInit(sdkInitializationResult: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(sdkInitializationResult)
                .flatMap {
                    bleScanner.scanner.subscribe { pool.put(it) }
                    Observable.just(it)

                }
                .flatMap { pool.onInit(it) }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap {
                    bleScanner.stopScan()
                    Observable.just(it)
                }
                .flatMap { pool.onTearDown(it) }
    }

}