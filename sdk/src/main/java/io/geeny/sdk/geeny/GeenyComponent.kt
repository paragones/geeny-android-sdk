package io.geeny.sdk.geeny

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.ble.BleComponent
import io.geeny.sdk.clients.mqtt.MqttComponent
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.netwok.NetworkClient
import io.geeny.sdk.geeny.auth.AuthenticationComponent
import io.geeny.sdk.geeny.cloud.CloudComponent
import io.geeny.sdk.geeny.cloud.api.repos.CloudThingInfo
import io.geeny.sdk.geeny.flow.Flower
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.geeny.sdk.geeny.things.TheThingType
import io.geeny.sdk.geeny.things.Thing
import io.geeny.sdk.geeny.things.emptyBleThing
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

class GeenyComponent(private val configuration: GeenyConfiguration,
                     private val keyValueStore: KeyValueStore,
                     private val ble: BleComponent,
                     private val context: Context,
                     private val mqtt: MqttComponent,
                     private val router: Router) {

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



    val getThingType: GetThingType  by lazy {
        GetThingType(cloud.thingTypeRepository, cloud.messageTypeRepository)
    }

    fun onInit(sdkInitializationResult: SdkInitializationResult): ObservableSource<SdkInitializationResult> = Observable.just(sdkInitializationResult)
            .flatMap { auth.onInit(it) }


    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> = Observable.just(result)
            .flatMap { auth.onTearDown(it) }

    fun getThing(serialNumber: String): Observable<Thing> =
            ble.localThingInfoRespository.get(serialNumber)
                    .flatMap {
                        Observable.zip<LocalThingInfo, CloudThingInfo, Thing>(
                                Observable.just(it),
                                cloud.thingRepository.get(serialNumber),
                                BiFunction { info, thing -> Thing(info, thing) }
                        )
                    }.defaultIfEmpty(emptyBleThing)

    fun register(localThingInfo: LocalThingInfo): Observable<Thing> =
            ble.localThingInfoRespository.save(localThingInfo)
                    .flatMap {
                        Observable.zip<LocalThingInfo, CloudThingInfo, Thing>(
                                Observable.just(it),
                                cloud.register(it),
                                BiFunction { local, cloud ->
                                    Thing(local, cloud)
                                })
                    }
                    .flatMap {
                        mqtt.create(it)
                                .doOnNext { GLog.d(TAG, "Created mqtt client") }
                    }

    fun getFlows(thing: Thing, routeType: RouteType): Observable<List<GeenyFlow>> =
            getThingType.get(thing.cloudThingInfo.thing_type)
                    .doOnNext { GLog.d(TAG, "Loaded cloudThingInfo type $it") }
                    .flatMap { getFlows(thing, it, routeType) }

    private fun getFlows(thing: Thing, thingType: TheThingType, routeType: RouteType): Observable<List<GeenyFlow>> =
        flow.getOrCreateRoutes(thing, thingType, routeType)



    companion object {
        val TAG = GeenyComponent::class.java.simpleName
    }
}