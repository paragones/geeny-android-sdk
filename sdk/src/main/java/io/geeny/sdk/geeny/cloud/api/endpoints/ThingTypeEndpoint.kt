package io.geeny.sdk.geeny.cloud.api.endpoints

import io.reactivex.Observable
import retrofit2.http.*


interface ThingTypeEndpoint {

    @GET("thingTypes/{thingTypeId}")
    fun get(@Path("thingTypeId") thingTypeId: String): Observable<ThingTypeResponse>

    @GET("thingTypes/{thingTypeId}/resources")
    fun listResources(@Path("thingTypeId") thingTypeId: String): Observable<ResourceListResponse>
}