package io.geeny.sdk.geeny.cloud.api.repos.thing

import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingPostBody
import io.geeny.sdk.geeny.cloud.api.endpoints.thingResponse2Thing
import io.geeny.sdk.geeny.cloud.api.repos.SimpleCache
import io.geeny.sdk.geeny.cloud.api.repos.Thing
import io.reactivex.Observable

class ThingRepository(val cache: SimpleCache<Thing>, val disk: ListDisk<Thing>, val endpoint: ThingEndpoint) {

    fun list(): Observable<List<Thing>> {
        return endpoint.list().map {
            it.data.map { thingResponse2Thing(it) }
        }
    }


    fun list(offset: Int, limit: Int): Observable<List<Thing>> {
        return endpoint.list(offset, limit).map {
            it.data.map { thingResponse2Thing(it) }
        }
    }

    fun get(id: String): Observable<Thing> {
        return cache.get(id)
                .switchIfEmpty(disk.get(id).flatMap { cache.save(it) })
    }

    fun save(thing: Thing): Observable<Thing> =
            disk.save(thing)
                    .flatMap { cache.save(it) }
                    .map { thing } // saving strips of the certificate information

    fun create(thingPostBody: ThingPostBody): Observable<Thing> {
        return endpoint.create(thingPostBody).map {
            thingResponse2Thing(it)
        }.flatMap { save(it) }
    }
}