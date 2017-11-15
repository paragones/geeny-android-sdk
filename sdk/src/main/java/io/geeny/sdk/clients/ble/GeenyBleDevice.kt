package io.geeny.sdk.clients.ble

import android.bluetooth.BluetoothDevice
import io.geeny.sdk.geeny.things.LocalThingInfo
import java.util.*

class GeenyBleDevice(
        val device: BluetoothDevice,
        private val rssi: Int,
        private val scanRecord: ByteArray,
        val deviceInfo: LocalThingInfo) {

    val serviceId: String
    val isGeenyDevice: Boolean
    val name: String?
    val address: String

    init {
        serviceId = if (scanRecord.isNotEmpty()) {
            val result = GeenyBleDevice.parseRecord(scanRecord)
            GeenyBleDevice.getServiceUUID(result)
        } else {
            ""
        }
        val hasLocalThingInfoLoaded = deviceInfo.isEmpty()
        isGeenyDevice = serviceId == GEENY_SERVICE_ID || !hasLocalThingInfoLoaded
        name = device.name
        address = device.address
    }


    companion object {

        val GEENY_SERVICE_ID = "0F050001-3225-44B1-B97D-D3274ACB29DE"
        val GEENY_CHARACTERISITIC_ID = "0F050002-3225-44B1-B97D-D3274ACB29DE"
        /*
            BLE Scan record type IDs
            data from:
            https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
        */
        val EBLE_FLAGS = 0x01//«Flags»	Bluetooth Core Specification:
        val EBLE_16BitUUIDInc = 0x02//«Incomplete List of 16-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_16BitUUIDCom = 0x03//«Complete List of 16-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_32BitUUIDInc = 0x04//«Incomplete List of 32-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_32BitUUIDCom = 0x05//«Complete List of 32-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_128BitUUIDInc = 0x06//«Incomplete List of 128-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_128BitUUIDCom = 0x07//«Complete List of 128-bit Service Class UUIDs»	Bluetooth Core Specification:
        val EBLE_SHORTNAME = 0x08//«Shortened Local Name»	Bluetooth Core Specification:
        val EBLE_LOCALNAME = 0x09//«Complete Local Name»	Bluetooth Core Specification:
        val EBLE_TXPOWERLEVEL = 0x0A//«Tx Power Level»	Bluetooth Core Specification:
        val EBLE_DEVICECLASS = 0x0D//«Class of Device»	Bluetooth Core Specification:
        val EBLE_SIMPLEPAIRHASH = 0x0E//«Simple Pairing Hash C»	Bluetooth Core Specification:​«Simple Pairing Hash C-192»	​Core Specification Supplement, Part A, section 1.6
        val EBLE_SIMPLEPAIRRAND = 0x0F//«Simple Pairing Randomizer R»	Bluetooth Core Specification:​«Simple Pairing Randomizer R-192»	​Core Specification Supplement, Part A, section 1.6
        val EBLE_DEVICEID = 0x10//«Device ID»	Device ID Profile v1.3 or later,«Security Manager TK Value»	Bluetooth Core Specification:
        val EBLE_SECURITYMANAGER = 0x11//«Security Manager Out of Band Flags»	Bluetooth Core Specification:
        val EBLE_SLAVEINTERVALRA = 0x12//«Slave Connection Interval Range»	Bluetooth Core Specification:
        val EBLE_16BitSSUUID = 0x14//«List of 16-bit Service Solicitation UUIDs»	Bluetooth Core Specification:
        val EBLE_128BitSSUUID = 0x15//«List of 128-bit Service Solicitation UUIDs»	Bluetooth Core Specification:
        val EBLE_SERVICEDATA = 0x16//«Service Data»	Bluetooth Core Specification:​«Service Data - 16-bit UUID»	​Core Specification Supplement, Part A, section 1.11
        val EBLE_PTADDRESS = 0x17//«Public Target Address»	Bluetooth Core Specification:
        val EBLE_RTADDRESS = 0x18;//«Random Target Address»	Bluetooth Core Specification:
        val EBLE_APPEARANCE = 0x19//«Appearance»	Bluetooth Core Specification:
        val EBLE_DEVADDRESS = 0x1B//«​LE Bluetooth Device Address»	​Core Specification Supplement, Part A, section 1.16
        val EBLE_LEROLE = 0x1C//«​LE Role»	​Core Specification Supplement, Part A, section 1.17
        val EBLE_PAIRINGHASH = 0x1D//«​Simple Pairing Hash C-256»	​Core Specification Supplement, Part A, section 1.6
        val EBLE_PAIRINGRAND = 0x1E//«​Simple Pairing Randomizer R-256»	​Core Specification Supplement, Part A, section 1.6
        val EBLE_32BitSSUUID = 0x1F//​«List of 32-bit Service Solicitation UUIDs»	​Core Specification Supplement, Part A, section 1.10
        val EBLE_32BitSERDATA = 0x20//​«Service Data - 32-bit UUID»	​Core Specification Supplement, Part A, section 1.11
        val EBLE_128BitSERDATA = 0x21//​«Service Data - 128-bit UUID»	​Core Specification Supplement, Part A, section 1.11
        val EBLE_SECCONCONF = 0x22//​«​LE Secure Connections Confirmation Value»	​Core Specification Supplement Part A, Section 1.6
        val EBLE_SECCONRAND = 0x23//​​«​LE Secure Connections Random Value»	​Core Specification Supplement Part A, Section 1.6​
        val EBLE_3DINFDATA = 0x3D//​​«3D Information Data»	​3D Synchronization Profile, v1.0 or later
        val EBLE_MANDATA = 0xFF//«Manufacturer Specific Data»	Bluetooth Core Specification:


        fun parseRecord(scanRecord: ByteArray): Map<Int, String> {
            val ret = HashMap<Int, String>()
            var index = 0
            while (index < scanRecord.size) {
                val length = scanRecord[index++].toInt()
                //Zero value indicates that we are done with the record now
                if (length == 0) break

                val type = scanRecord[index].toInt()
                //if the type is zero, then we are pass the significant section of the data,
                // and we are thud done
                if (type == 0) break

                val data = Arrays.copyOfRange(scanRecord, index + 1, index + length)
                if (data != null && data.isNotEmpty()) {
                    val hex = StringBuilder(data.size * 2)
                    // the data appears to be there backwards
                    for (bb in data.indices.reversed()) {
                        hex.append(String.format("%02X", data[bb]))
                    }
                    ret.put(type, hex.toString())
                }
                index += length
            }

            return ret
        }


        fun getServiceUUID(record: Map<Int, String>): String {
            var ret = ""
            // for example: 0105FACB00B01000800000805F9B34FB --> 010510ee-0000-1000-8000-00805f9b34fb
            if (record.containsKey(EBLE_128BitUUIDCom)) {
                val tmpString = record[EBLE_128BitUUIDCom].toString()
                ret = tmpString.substring(0, 8) + "-" + tmpString.substring(8, 12) + "-" + tmpString.substring(12, 16) + "-" + tmpString.substring(16, 20) + "-" + tmpString.substring(20, tmpString.length)
            } else if (record.containsKey(EBLE_32BitUUIDCom)) {
                ret = record[EBLE_32BitUUIDCom].toString() + "-0000-1000-8000-00805f9b34fb"
            }
            return ret
        }
    }
}