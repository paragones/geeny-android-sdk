package io.geeny.sdk.clients.ble

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import io.geeny.sdk.common.GLog
import io.geeny.sdk.geeny.LocalThingInfoRepository
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject


class BleScanner(val context: Context, private val localThingInfoRepository: LocalThingInfoRepository) {

    val adapter: BluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    val scanner: PublishSubject<GeenyBleDevice> = PublishSubject.create()

    private val callback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        if (device!!.name != null) {
            val deviceInfo: LocalThingInfo= localThingInfoRepository.getByAddress(device.address)
            val gbd = GeenyBleDevice(device, rssi, scanRecord, deviceInfo)
            scanner.onNext(gbd)
        }
    }


    fun createRemoteDevice(address: String): Observable<GeenyBleDevice> = Observable.create<BluetoothDevice> { subscriber ->
        val device = adapter.getRemoteDevice(address)
        GLog.d(TAG, "Device loaded: " + device)
        subscriber.onNext(device)
        subscriber.onComplete()
    }.flatMap { bluetoothDevice ->
        Observable.zip<BluetoothDevice, LocalThingInfo, GeenyBleDevice>(
                Observable.just(bluetoothDevice),
                localThingInfoRepository.loadByAddress(address),
                BiFunction { device, deviceInfo ->
                    GeenyBleDevice(device, 0, kotlin.ByteArray(0), deviceInfo)
                })
    }

    var availableDevice: MutableMap<String, GeenyBleDevice> = mutableMapOf()

    fun isBluetoothEnabled(): Boolean {
        return adapter.isEnabled
    }

    fun enableBluetooth(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun startScan() {
        adapter.startLeScan(callback)
    }

    fun stopScan() {
        adapter.stopLeScan(callback)
    }

    companion object {
        val TAG = BleScanner::class.java.simpleName
        val REQUEST_ENABLE_BT = 1240
    }
}
