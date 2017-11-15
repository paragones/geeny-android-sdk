package io.geeny.sdk.geeny.auth

import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.common.netwok.Http
import io.geeny.sdk.common.netwok.NetworkClient
import io.geeny.sdk.common.netwok.Request
import io.geeny.sdk.common.netwok.RequestBody
import io.reactivex.Observable
import org.json.JSONObject
import java.lang.Exception
import java.nio.charset.Charset

object SignIn {

    fun signInWithCredentials(credentials: Credentials, networkClient: NetworkClient, configuration: GeenyConfiguration): Observable<AuthToken> =
            Observable.just(credentials)
                    .flatMap {
                        val body = credentials.toJSON().toString()
                        val requestBody = RequestBody.create(Http.MEDIA_TYPE_APPLICATION_JSON, body.toByteArray(Charset.forName("UTF-8")))

                        val request: Request = Request.Builder()
                                .header("X-CSRFToken", "nKUJhiPHzt0L0Iv8zRGsxzU3JvXe7YNYJ0lqjLhhW54QZBfNddqBx0ZQoiGkeLrd")
                                .method(Http.METHOD_POST)
                                .accept(Http.MEDIA_TYPE_APPLICATION_JSON)
                                .body(requestBody)
                                .url(configuration.environment.geenyConnectBaseUrl())
                                .build()
                        val response = networkClient.execute(request)

                        if (response.isSuccessful) {
                            val token = JSONObject(response.responseBody).getString("token")
                            Observable.just(AuthToken(token))
                        } else {
                            Observable.error(Exception("User couldn't be logged in!"))
                        }
                    }

}