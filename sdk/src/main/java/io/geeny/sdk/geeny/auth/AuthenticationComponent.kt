package io.geeny.sdk.geeny.auth

import android.content.Context
import android.content.Intent
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.netwok.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class AuthenticationComponent(
        val configuration: GeenyConfiguration,
        private val networkClient: NetworkClient,
        private val keyValueStore: KeyValueStore) {
    val state: Stream<State> = Stream()

    private val tokenManager: TokenManager by lazy {
        TokenManager(keyValueStore)
    }

    init {
        state.set(State.SIGNED_OUT)
    }

    fun token(): String = tokenManager.token()

    fun isSignedIn(): Observable<Boolean> = Observable.just(state.value!!).map { it == State.SIGNED_IN }

    fun signInWithCredentials(credentials: Credentials): Observable<State> =
            SignIn.signInWithCredentials(credentials, networkClient, configuration)
                    .filter { !it.isEmpty() }
                    .flatMap { tokenManager.onSignedIn(it) }
                    .map { State.SIGNED_IN }
                    .switchIfEmpty { Observable.just(State.SIGNED_OUT) }
                    .doOnNext { state.set(it) }

    fun signOut(): Observable<State> {
        state.set(State.SIGN_OUT_INTENT)

        return tokenManager.signOut()
                .doOnNext { state.set(State.SIGNED_OUT) }
                .map { State.SIGNED_OUT }
    }

    fun signIn(context: Context): Observable<Boolean> = Observable.create {
        context.startActivity(configuration.environment.signInIntent())
        it.onNext(true)
        it.onComplete()
    }

    /*
        This is the correct way to do it (OAuth2) but it is not supported by backend....
     */
    fun getAuthToken(code: String): Observable<String> = Observable.create {

        val url = URL(configuration.environment.geenyConnectBaseUrl())
        val conn = url.openConnection() as HttpsURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "POST"
        conn.doInput = true
        conn.doOutput = true


        val params = ArrayList<NameValuePair>()
        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(HttpClient.getQuery(params))
        writer.flush()
        writer.close()
        os.close()

        conn.connect()
        val responseCode = conn.responseCode

        var response = ""
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            var line: String
            val br = BufferedReader(InputStreamReader(conn.inputStream))
            line = br.readLine()

            while (line.isNotEmpty()) {
                response += line
                line = br.readLine()
            }
        }

    }

    fun onNewIntent(intent: Intent): Observable<Boolean> =
            Observable.just(intent)
                    .map { getCode(it) }
                    .filter { it.isNotEmpty() }
                    .flatMap { getAuthToken(it) }
                    .map { true }
                    .defaultIfEmpty(false)


    private fun getCode(intent: Intent): String {
        if (intent.data != null && intent.data.getQueryParameter("code") != null) {
            return intent.data.getQueryParameter("code")
        }
        return ""
    }

    fun onInit(sdkInitializationResult: SdkInitializationResult): ObservableSource<SdkInitializationResult> {
        return Observable.just(sdkInitializationResult)
                .flatMap { tokenManager.onInit(it) }
                .doOnNext {

                    if (tokenManager.isSignedIn()) {
                        state.set(State.SIGNED_IN)
                    } else {
                        state.set(State.SIGNED_OUT)
                    }
                }

    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> {
        return Observable.just(result)
                .flatMap { tokenManager.onTearDown(result) }
    }

    enum class State {
        SIGNED_IN,
        RECEIVED_CODE,
        SIGN_OUT_INTENT,
        SIGNED_OUT
    }
}