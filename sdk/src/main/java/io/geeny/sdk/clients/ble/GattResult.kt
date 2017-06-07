package io.geeny.sdk.clients.ble

import android.bluetooth.BluetoothGattCharacteristic
import io.geeny.sdk.common.toHex
import java.text.SimpleDateFormat
import java.util.*

data class GattResult(val characteristic: BluetoothGattCharacteristic, val type: GattResultType, val property: CharacteristicProperty, val date: Long = System.currentTimeMillis()) {
    fun formattedDate(): String = dt.format(Date(date)).toString()
    fun formattedValue(): String = currentValue.toHex()

    fun id(): String = characteristic.uuid.toString()
    val currentValue = characteristic.value

    companion object {
        val dt = SimpleDateFormat("MM-dd hh:mm:ss")
    }
}

enum class GattResultType(val value: Int) {
    UNKNOWN_GATT_RESULT(-1),

    /** A GATT operation completed successfully  */
    GATT_SUCCESS(0),

    /** GATT read operation is not permitted  */
    GATT_READ_NOT_PERMITTED(0x2),

    /** GATT write operation is not permitted  */
    GATT_WRITE_NOT_PERMITTED(0x3),

    /** Insufficient authentication for a given operation  */
    GATT_INSUFFICIENT_AUTHENTICATION(0x5),

    /** The given request is not supported  */
    GATT_REQUEST_NOT_SUPPORTED(0x6),

    /** Insufficient encryption for a given operation  */
    GATT_INSUFFICIENT_ENCRYPTION(0xf),

    /** A read or write operation was requested with an invalid offset  */
    GATT_INVALID_OFFSET(0x7),

    /** A write operation exceeds the maximum length of the attribute  */
    GATT_INVALID_ATTRIBUTE_LENGTH(0xd),

    /** A remote device connection is congested.  */
    GATT_CONNECTION_CONGESTED(0x8f),

    /** A GATT operation failed, errors other than the above  */
    GATT_FAILURE(0x101);


    companion object {

        fun get(value: Int): GattResultType {
            val result = GattResultType.values().filter { value == it.value }

            return if (result.isEmpty()) {
                UNKNOWN_GATT_RESULT
            } else {
                result[0]
            }
        }
    }
}