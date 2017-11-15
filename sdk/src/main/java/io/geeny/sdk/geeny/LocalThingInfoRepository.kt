package io.geeny.sdk.geeny

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.common.SimpleCache
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.geeny.sdk.geeny.things.emptyDeviceInfo
import io.reactivex.Observable
import org.json.JSONObject
import java.util.*

class LocalThingInfoRepository(private val cache: SimpleCache<LocalThingInfo>, private val disk: DeviceInfoDisk) {

    fun save(localThingInfo: LocalThingInfo): Observable<LocalThingInfo> =
            Observable.just(localThingInfo)
                    .flatMap { disk.save(it) }
                    .flatMap { cache.save(it) }

    fun get(serialNumber: String): Observable<LocalThingInfo> =
            cache.get(serialNumber)
                    .switchIfEmpty(disk.get(serialNumber).flatMap { cache.save(it) })

    fun list(): Observable<List<LocalThingInfo>> =
            cache.list().filter { it.isNotEmpty() }
                    .switchIfEmpty(disk.list().flatMap { cache.save(it) })

    fun loadByAddress(address: String): Observable<LocalThingInfo> {
        return list().map {
            var deviceInfo = emptyDeviceInfo()
            it
                    .filter { it.address == address }
                    .forEach { deviceInfo = it }
            deviceInfo
        }

    }

    fun getByAddress(address: String): LocalThingInfo = loadByAddress(address).blockingSingle()
}

class DeviceInfoCache : SimpleCache<LocalThingInfo>() {
    override fun id(value: LocalThingInfo): String = value.serialNumber.toString()
}

class DeviceInfoDisk(keyValueStore: KeyValueStore) : ListDisk<LocalThingInfo>(keyValueStore, DeviceInfoConverter, "list_id_device_info")

object DeviceInfoConverter : JSONConverter<LocalThingInfo> {
    private val JSON_KEY_SERIAL_NUMBER = "JSON_KEY_SERIAL_NUMBER"
    private val JSON_KEY_DEVICE_NAME = "JSON_KEY_DEVICE_NAME"
    private val JSON_KEY_ADDRESS = "JSON_KEY_ADDRESS"
    private val JSON_KEY_PROTOCOL_VERSION = "JSON_KEY_PROTOCOL_VERSION"
    private val JSON_KEY_THING_TYPE_ID = "JSON_KEY_THING_TYPE_ID"

    override fun id(value: LocalThingInfo): String = value.serialNumber.toString()

    override fun toJSON(value: LocalThingInfo): JSONObject {
        val json = JSONObject()
        json.put(JSON_KEY_SERIAL_NUMBER, id(value))
        json.put(JSON_KEY_DEVICE_NAME, value.deviceName)
        json.put(JSON_KEY_ADDRESS, value.address)
        json.put(JSON_KEY_PROTOCOL_VERSION, value.protocolVersion)
        json.put(JSON_KEY_THING_TYPE_ID, value.thingTypeId)
        return json
    }

    override fun fromJSON(json: JSONObject): LocalThingInfo {

        val deviceName: String = json.getString(JSON_KEY_DEVICE_NAME)
        val address: String = json.getString(JSON_KEY_ADDRESS)
        val protocolVersion: Int = json.getInt(JSON_KEY_PROTOCOL_VERSION)
        val serialNumber: String = json.getString(JSON_KEY_SERIAL_NUMBER)
        val thingTypeId: String = json.getString(JSON_KEY_THING_TYPE_ID)

        return LocalThingInfo(
                deviceName, address, protocolVersion, serialNumber, thingTypeId
        )
    }

}