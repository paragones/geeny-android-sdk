package io.geeny.sdk.clients.ble

import android.content.Context
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.common.GLog
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

open class BleClientPool(private val context: Context, private val bleScanner: BleScanner) {

    private val map: MutableMap<String, BleClient> = HashMap()
    private fun toList(): List<BleClient> = map.toList().map { it.second }
    private val availableDevicesStream: PublishSubject<List<BleClient>> = PublishSubject.create()


    fun availableDevices(): Observable<List<BleClient>> = Observable.merge(Observable.just(toList()), availableDevicesStream)

    fun put(device: GeenyBleDevice) {
        if (!map.containsKey(device.address)) {
            map.put(device.address, BleClient(device.address, device))
            availableDevicesStream.onNext(toList())
        } else if (!map[device.address]!!.isAvailable()) {
            map[device.address]!!.gbd = device
        }
    }

    open fun getOrCreate(address: String): Observable<BleClient> =
            get(address)
                    .switchIfEmpty (
                      //  GLog.d(TAG, "Creating remote device for $address")
                        bleScanner.createRemoteDevice(address)
                                .map {
                                    GLog.d(TAG, "remote device $address created....")
                                    val client = BleClient(address, GeenyBleDevice(it, 0, kotlin.ByteArray(0)))
                                    map[address] = client
                                    client
                                }
                    )



    fun get(address: String): Observable<BleClient> = Observable.create { subscriber ->
        if (map.containsKey(address)) {
            subscriber.onNext(map[address]!!)
        }
        subscriber.onComplete()
    }

    fun tearDown() {
        toList().forEach { it.disconnect() }
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
        val TAG = BleClientPool::class.java.simpleName
    }
}