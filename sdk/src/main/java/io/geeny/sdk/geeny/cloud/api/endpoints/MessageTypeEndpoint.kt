package io.geeny.sdk.geeny.cloud.api.endpoints

import io.reactivex.Observable
import retrofit2.http.*

interface MessageTypeEndpoint {
    @GET("messageTypes/{id}")
    fun get(@Path("id") id: String): Observable<MessageTypeResponse>


    @GET("messageTypes")
    fun list(): Observable<MessageTypeListResponse>

    @GET("messageType")
    fun list(@Query("offset") offset: Int, @Query("limit") limit: Int): io.reactivex.Observable<MessageTypeListResponse>
}