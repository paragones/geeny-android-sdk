package io.geeny.sdk.geeny

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.mqtt.MqttComponent
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.netwok.NetworkClient
import io.geeny.sdk.geeny.auth.AuthenticationComponent
import io.geeny.sdk.geeny.cloud.CloudComponent
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.geeny.sdk.geeny.cloud.api.repos.Thing
import io.geeny.sdk.geeny.deviceinfo.DeviceInfoCache
import io.geeny.sdk.geeny.deviceinfo.DeviceInfoDisk
import io.geeny.sdk.geeny.deviceinfo.DeviceInfoRepository
import io.geeny.sdk.geeny.flow.Flower
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.geeny.things.BleThing
import io.geeny.sdk.geeny.things.TheThingType
import io.geeny.sdk.routing.router.Router
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

class GeenyComponent(val configuration: GeenyConfiguration,
                     private val keyValueStore: KeyValueStore,
                     context: Context,
                     val mqtt: MqttComponent,
                     router: Router) {

    val auth: AuthenticationComponent by lazy {
        AuthenticationComponent(configuration, networkClient, keyValueStore)
    }

    val cloud: CloudComponent by lazy {
        CloudComponent(configuration, auth, keyValueStore, context)
    }

    val flow: Flower by lazy {
        Flower(configuration, keyValueStore, router)
    }

    val networkClient: NetworkClient by lazy {
        NetworkClient()
    }

    val deviceInfoRespository: DeviceInfoRepository by lazy {
        DeviceInfoRepository(DeviceInfoCache(), DeviceInfoDisk(keyValueStore))
    }

    val getThing: GetThing  by lazy {
        GetThing(deviceInfoRespository, cloud.thingRepository)
    }


    val getThingType: GetThingType  by lazy {
        GetThingType(cloud.thingTypeRepository, cloud.messageTypeRepository)
    }

    fun onInit(sdkInitializationResult: SdkInitializationResult): ObservableSource<SdkInitializationResult> = Observable.just(sdkInitializationResult)
            .flatMap { auth.onInit(it) }


    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> = Observable.just(result)
            .flatMap { auth.onTearDown(it) }

    fun getThing(serialNumber: String) = getThing.get(serialNumber)

    fun register(deviceInfo: DeviceInfo): Observable<BleThing> =
            deviceInfoRespository.save(deviceInfo)
                    .flatMap {
                        Observable.zip<DeviceInfo, Thing, BleThing>(
                                Observable.just(it),
                                cloud.register(it),
                                BiFunction { local, cloud ->
                                    BleThing(local, cloud)
                                })
                    }
                    .flatMap {
                        mqtt.create(it)
                                .doOnNext { GLog.d(TAG, "Created mqtt client") }
                    }

    fun getFlows(bleThing: BleThing) =
            getThingType.get(bleThing.thing.thing_type)
                    .doOnNext { GLog.d(TAG, "Loaded thing type $it") }
                    .flatMap { getFlows(bleThing, it) }

    private fun getFlows(bleThing: BleThing, thingType: TheThingType): Observable<List<GeenyFlow>> =
        flow.getOrCreateRoutes(bleThing, thingType)


    companion object {
        val TAG = GeenyComponent::class.java.simpleName
    }
}