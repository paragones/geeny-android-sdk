package io.geeny.sdk.geeny.cloud.api.repos.thing

import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingPostBody
import io.geeny.sdk.geeny.cloud.api.endpoints.thingResponse2Thing
import io.geeny.sdk.geeny.cloud.api.repos.SimpleCache
import io.geeny.sdk.geeny.cloud.api.repos.CloudThingInfo
import io.reactivex.Observable

class ThingRepository(val cache: SimpleCache<CloudThingInfo>, val disk: ListDisk<CloudThingInfo>, val endpoint: ThingEndpoint) {

    fun list(): Observable<List<CloudThingInfo>> {
        return endpoint.list().map {
            it.data.map { thingResponse2Thing(it) }
        }
    }


    fun list(offset: Int, limit: Int): Observable<List<CloudThingInfo>> {
        return endpoint.list(offset, limit).map {
            it.data.map { thingResponse2Thing(it) }
        }
    }

    fun get(id: String): Observable<CloudThingInfo> {
        return cache.get(id)
                .switchIfEmpty(disk.get(id).flatMap { cache.save(it) })
    }

    fun save(cloudThingInfo: CloudThingInfo): Observable<CloudThingInfo> =
            disk.save(cloudThingInfo)
                    .flatMap { cache.save(it) }
                    .map { cloudThingInfo } // saving strips of the certificate information

    fun create(thingPostBody: ThingPostBody): Observable<CloudThingInfo> {
        return endpoint.create(thingPostBody).map {
            thingResponse2Thing(it)
        }.flatMap { save(it) }
    }
}