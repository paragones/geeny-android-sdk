package io.geeny.sdk.clients.ble

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import io.geeny.sdk.clients.ThingInfo
import io.geeny.sdk.clients.emptyThingInfo
import io.geeny.sdk.common.GLog
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class BleScanner(val context: Context) {

    val adapter: BluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    val scanner: PublishSubject<GeenyBleDevice> = PublishSubject.create()

    val updatedList: PublishSubject<List<BluetoothDevice>> = PublishSubject.create()

    private val callback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        if (device!!.name != null) {
            val gbd = GeenyBleDevice(device, rssi, scanRecord)
            scanner.onNext(gbd)
        }
    }

    fun createRemoteDevice(address: String): Observable<BluetoothDevice> = Observable.create { subscriber ->
        val device = adapter.getRemoteDevice(address)
        GLog.d(TAG, "Device loaded: " + device)
        subscriber.onNext(device)
        subscriber.onComplete()
    }

    var availableDevice: MutableMap<String, GeenyBleDevice> = mutableMapOf()

    fun isBluetoothEnabled(): Boolean {
        return adapter.isEnabled
    }

    fun enableBluetooth(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun gatt(peripheralId: String): ThingInfo {
        return emptyThingInfo()
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
