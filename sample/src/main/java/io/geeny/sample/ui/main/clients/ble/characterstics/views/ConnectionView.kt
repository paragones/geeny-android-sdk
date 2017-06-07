package io.geeny.sample.ui.main.clients.ble.characterstics.views

import android.bluetooth.BluetoothGattCharacteristic
import io.geeny.sdk.clients.ble.BleClient

interface ConnectionView {
    fun bind(connection: BleClient, characteristic: BluetoothGattCharacteristic)
}