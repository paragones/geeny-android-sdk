package io.geeny.sdk.geeny

import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.geeny.sdk.geeny.cloud.api.repos.Thing
import io.geeny.sdk.geeny.cloud.api.repos.emptyThing
import io.geeny.sdk.geeny.deviceinfo.DeviceInfoRepository
import io.geeny.sdk.geeny.cloud.api.repos.thing.ThingRepository
import io.geeny.sdk.geeny.things.BleThing
import io.geeny.sdk.geeny.things.emptyBleThing
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class GetThing(
        private val deviceInfoRepository: DeviceInfoRepository,
        private val thingRepository: ThingRepository
) {

    fun get(serialNumber: String): Observable<BleThing> =
            deviceInfoRepository.get(serialNumber)
                    .flatMap {
                        Observable.zip<DeviceInfo, Thing, BleThing>(
                                Observable.just(it),
                                thingRepository.get(serialNumber),
                                BiFunction { info, thing -> BleThing(info, thing) }
                        )
                    }.defaultIfEmpty(emptyBleThing)

}