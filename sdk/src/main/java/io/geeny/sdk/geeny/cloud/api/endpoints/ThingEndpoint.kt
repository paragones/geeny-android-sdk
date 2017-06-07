package io.geeny.sdk.geeny.cloud.api.endpoints

import retrofit2.http.*

interface ThingEndpoint {
    @GET("things")
    fun list(): io.reactivex.Observable<ThingListResponse>

    @GET("things")
    fun list(@Query("offset") offset: Int, @Query("limit") limit: Int): io.reactivex.Observable<ThingListResponse>

    @GET("things/{id}")
    fun get(@Path("id") id: String): io.reactivex.Observable<ThingResponse>

    @POST("things")
    fun create(@Body thingPostBody: io.geeny.sdk.geeny.cloud.api.endpoints.ThingPostBody): io.reactivex.Observable<ThingResponse>
}
