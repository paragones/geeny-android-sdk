package io.geeny.sdk.geeny.deviceinfo

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.common.SimpleCache
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.reactivex.Observable
import org.json.JSONObject
import java.util.*

class DeviceInfoRepository(private val cache: DeviceInfoCache, private val disk: DeviceInfoDisk) {

    fun save(deviceInfo: DeviceInfo): Observable<DeviceInfo> =
            Observable.just(deviceInfo)
                    .flatMap { disk.save(it) }
                    .flatMap { cache.save(it) }

    fun get(serialNumber: String): Observable<DeviceInfo> =
            cache.get(serialNumber)
                    .switchIfEmpty(disk.get(serialNumber).flatMap { cache.save(it) })

    fun list(): Observable<List<DeviceInfo>> =
            cache.list().filter { it.isNotEmpty() }
                    .switchIfEmpty(disk.list().flatMap { cache.save(it) })
}

class DeviceInfoCache : SimpleCache<DeviceInfo>() {
    override fun id(value: DeviceInfo): String = value.serialNumber.toString()
}

class DeviceInfoDisk(keyValueStore: KeyValueStore) : ListDisk<DeviceInfo>(keyValueStore, DeviceInfoConverter, "list_id_device_info")

object DeviceInfoConverter : JSONConverter<DeviceInfo> {
    private val JSON_KEY_SERIAL_NUMBER = "JSON_KEY_SERIAL_NUMBER"
    private val JSON_KEY_DEVICE_NAME = "JSON_KEY_DEVICE_NAME"
    private val JSON_KEY_ADDRESS = "JSON_KEY_ADDRESS"
    private val JSON_KEY_PROTOCOL_VERSION = "JSON_KEY_PROTOCOL_VERSION"
    private val JSON_KEY_THING_TYPE_ID = "JSON_KEY_THING_TYPE_ID"

    override fun id(value: DeviceInfo): String = value.serialNumber.toString()

    override fun toJSON(value: DeviceInfo): JSONObject {
        val json = JSONObject()
        json.put(JSON_KEY_SERIAL_NUMBER, id(value))
        json.put(JSON_KEY_DEVICE_NAME, value.deviceName)
        json.put(JSON_KEY_ADDRESS, value.address)
        json.put(JSON_KEY_PROTOCOL_VERSION, value.protocolVersion)
        json.put(JSON_KEY_THING_TYPE_ID, value.thingTypeId)
        return json
    }

    override fun fromJSON(json: JSONObject): DeviceInfo {

        val deviceName: String = json.getString(JSON_KEY_DEVICE_NAME)
        val address: String = json.getString(JSON_KEY_ADDRESS)
        val protocolVersion: Int = json.getInt(JSON_KEY_PROTOCOL_VERSION)
        val serialNumber: UUID = UUID.fromString(json.getString(JSON_KEY_SERIAL_NUMBER))
        val thingTypeId: UUID = UUID.fromString(json.getString(JSON_KEY_THING_TYPE_ID))

        return DeviceInfo(
                deviceName, address, protocolVersion, serialNumber, thingTypeId
        )
    }

}