package io.geeny.sdk.clients.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.*
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


open class BleClient(val address: String, var gbd: GeenyBleDevice) : BluetoothGattCallback() {

    // Streams
    private val availability: BehaviorSubject<AvailabilityState> = BehaviorSubject.create()
    private val services: PublishSubject<List<BluetoothGattService>> = PublishSubject.create()
    private val characteristics: MutableMap<String, BluetoothGattCharacteristic> = HashMap()

    private val connectionStream: Stream<ConnectionState> = Stream()
    private val deviceInfoStream: Stream<LocalThingInfo> = Stream()

    fun connection(): Observable<ConnectionState> = connectionStream.connect()
    fun geenyInformation(): Observable<LocalThingInfo> = deviceInfoStream.connect()

    fun services(): Observable<List<BluetoothGattService>> = Observable.merge(Observable.just(gattService), services)
    private var gattService: List<BluetoothGattService> = ArrayList()

    private val characteristicValue: MutableMap<String, BehaviorSubject<GattResult>> = HashMap()
    private val characteristicCallback: MutableMap<String, ReplaySubject<GattResult>> = HashMap()
    private val characteristicConnectionState: MutableMap<String, BehaviorSubject<ConnectionState>> = HashMap()

    fun device(): BluetoothDevice? = gbd.device
    fun address() = address
    fun name() = gbd.name ?: "unnamed"

    var gatt: BluetoothGatt? = null

    var hasServiceDiscovered: Boolean = false

    var geenyServiceIdFound = false
    fun isGeenyDevice(): Boolean = gbd.isGeenyDevice || geenyServiceIdFound
    fun connectionStatus(): ConnectionState = connectionStream.value!!
    fun isConnected(): Boolean = connectionStream.value == ConnectionState.CONNECTED

    init {
        connectionStream.set(ConnectionState.DISCONNECTED)
        if (!gbd.deviceInfo.isEmpty()) {
            deviceInfoStream.set(gbd.deviceInfo)
        }
    }


    fun hasServiceLoaded(): Boolean = hasServiceDiscovered

    fun isAvailable(): Boolean {
        return availability.value == AvailabilityState.AVAILABLE
    }

    fun connect(context: Context) {
        GLog.i(TAG, "Connecting device....")
        if (device() != null) {
            gatt = device()?.connectGatt(context, false, this)
            connectionStream.set(ConnectionState.CONNECTING)
        } else {
            GLog.e(TAG, "Trying to connectSecure to an unavailable device", IllegalStateException("Trying to connectSecure to an unavailable device"))
        }
    }

    fun disconnect() {
        gatt?.disconnect()
    }

    fun characteristicById(id: String): BluetoothGattCharacteristic = characteristics[id]!!

    fun value(cid: String): Observable<GattResult> {
        if (!characteristicValue.containsKey(cid)) {
            characteristicValue[cid] = BehaviorSubject.create()
        }

        return characteristicValue[cid]!!
    }

    fun callback(characteristic: BluetoothGattCharacteristic): Observable<GattResult> {
        val cid = characteristic.uuid.toString()
        if (!characteristicCallback.containsKey(cid)) {
            characteristicCallback[cid] = ReplaySubject.create()
        }

        return characteristicCallback[cid]!!
    }

    fun write(characteristic: BluetoothGattCharacteristic, byteArray: ByteArray) {
        characteristic.value = byteArray
        gatt!!.writeCharacteristic(characteristic)
    }

    fun read(characteristic: BluetoothGattCharacteristic) {
        gatt!!.readCharacteristic(characteristic)
    }

    fun notify(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        GLog.i(TAG, "Enabling notification $enable for characteristic ${characteristic.uuid.toString()}")
        val descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)

        if (enable) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            characteristicState(characteristic).onNext(ConnectionState.CONNECTED)
        } else {
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            characteristicState(characteristic).onNext(ConnectionState.DISCONNECTED)
        }

        gatt!!.writeDescriptor(descriptor)
        gatt!!.setCharacteristicNotification(characteristic, true)
    }

    fun notify(characteristicID: String, enable: Boolean) {
        GLog.d(TAG, "Trying to connect $characteristicID")
        notify(characteristicById(characteristicID), enable)
    }

    fun characteristicState(characteristic: BluetoothGattCharacteristic): BehaviorSubject<ConnectionState> {
        val cid = characteristic.uuid.toString()
        if (!characteristicConnectionState.containsKey(cid)) {
            characteristicConnectionState[cid] = BehaviorSubject.create()
            characteristicConnectionState[cid]!!.onNext(ConnectionState.DISCONNECTED)
        }

        return characteristicConnectionState[cid]!!
    }

    private fun onReceivedGeenyCharacteristicPayload(result: GattResult) {
        val hex = result.currentValue.toHex()
        GLog.d(TAG, "onReceivedGeenyCharacteristicPayload:\n $hex")
        val serviceInformation = extractLocalThingInformation(name() ?: "no name specified", address(), result.currentValue)
        GLog.d(TAG, "extracted $serviceInformation")
        deviceInfoStream.set(serviceInformation)
    }

    private fun onResultReceived(result: GattResult) {
        GLog.d(TAG, "onResultReceived $result")

        if (result.id() == GeenyBleDevice.GEENY_CHARACTERISITIC_ID.toLowerCase()) {
            onReceivedGeenyCharacteristicPayload(result)
        }

        val cid = result.characteristic.uuid.toString()
        // create callback stream if necessary
        if (!characteristicCallback.containsKey(cid)) {
            characteristicCallback[cid] = ReplaySubject.create()
        }

        characteristicCallback[cid]!!.onNext(result)

        // if it is a success update the value
        if (result.type == GattResultType.GATT_SUCCESS) {
            // create value
            if (!characteristicValue.containsKey(cid)) {
                characteristicValue[cid] = BehaviorSubject.create()
            }

            characteristicValue[cid]!!.onNext(result)
        }
    }

    /*
        Android Service callbacks
     */

    override fun onConnectionStateChange(gatt: BluetoothGatt,
                                         status: Int,
                                         newState: Int) {
        val connectionStatus = when (newState) {
            BluetoothProfile.STATE_CONNECTED -> ConnectionState.CONNECTED
            BluetoothProfile.STATE_DISCONNECTED -> ConnectionState.DISCONNECTED
            BluetoothProfile.STATE_CONNECTING -> ConnectionState.CONNECTING
            else -> ConnectionState.DISCONNECTED
        }

        GLog.d(TAG, "Connection status of lient has changed: " + connectionStatus)

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices()
        }
        connectionStream.set(connectionStatus)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt,
                                      status: Int) {
        Log.w(TAG, "Service discovered: " + status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            hasServiceDiscovered = true
            gattService = gatt.services
            gattService
                    .flatMap { it.characteristics }
                    .forEach { characteristics[it.uuid.toString()] = it }


            gattService.forEach {
                GLog.d(TAG, "Available service: " + it.uuid.toString())
                if (it.uuid.toString() == GeenyBleDevice.GEENY_SERVICE_ID.toLowerCase()) {
                    geenyServiceIdFound = true
                }
            }
            if (isGeenyDevice()) {
                GLog.d(TAG, "triggering read on Geeny characteristic")
                val geeny_c = characteristics[GeenyBleDevice.GEENY_CHARACTERISITIC_ID.toLowerCase()]

                if (geeny_c != null) {
                    GLog.d(TAG, "Reading ${geeny_c}")
                    read(geeny_c)
                }
            }
            services.onNext(gatt.services)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        onResultReceived(GattResult(characteristic, GattResultType.get(status), CharacteristicProperty.PROPERTY_READ))
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        onResultReceived(GattResult(characteristic, GattResultType.get(status), CharacteristicProperty.PROPERTY_WRITE))
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        onResultReceived(GattResult(characteristic, GattResultType.GATT_SUCCESS, CharacteristicProperty.PROPERTY_NOTIFY))
    }

    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {}
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {}
    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {}
    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {}
    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {}

    companion object {
        private val TAG = BleClient::class.java.simpleName
        private val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun extractLocalThingInformation(name: String,
                                         address: String,
                                         bytes: ByteArray): LocalThingInfo {
            val protocol = bytes.sliceArray(IntRange(0, 1))
            val serialNumber = bytes.sliceArray(IntRange(2, 17))
            val thingType = bytes.sliceArray(IntRange(18, 33))

            serialNumber.reverse()
            thingType.reverse()


            return LocalThingInfo(
                    name,
                    address,
                    TypeConverters.bytesToIntDynamic(protocol, ByteOrder.LITTLE_ENDIAN),
                    serialNumber.toUUID().toString(),
                    thingType.toUUID().toString()
            )
        }
    }
}
