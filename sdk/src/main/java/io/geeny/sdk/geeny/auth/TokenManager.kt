package io.geeny.sdk.geeny.auth

import android.util.Log
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.reactivex.Observable
import io.reactivex.ObservableSource

class TokenManager(private val keyValueStore: KeyValueStore) {

    var currentToken: AuthToken = emptyToken()

    fun token(): String = currentToken.token

    fun isSignedIn(): Boolean = !currentToken.isEmpty()

    fun onSignedIn(token: AuthToken): Observable<AuthToken> =
            keyValueStore.save(KEY_AUTH_TOKEN, token.token)
                    .doOnNext { currentToken = token }
                    .map { token }

    fun signOut(): Observable<Boolean> {
        return if (isSignedIn()) {
            keyValueStore.delete(KEY_AUTH_TOKEN).doOnNext { currentToken = emptyToken() }
        } else {
            Observable.just(false)
        }
    }


    fun onInit(result: SdkInitializationResult): Observable<SdkInitializationResult> {
        return Observable.just(result)
                .flatMap { r ->
                    keyValueStore.get(KEY_AUTH_TOKEN)
                            .doOnNext {
                                GLog.d(TAG, "restored token: $it")
                                currentToken = AuthToken(it)
                            }
                            .map { r.copy(isSignedIn = true) }
                            .defaultIfEmpty(r)
                }
    }

    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> =
            Observable.just(result)

    companion object {
        val TAG = TokenManager::class.java.simpleName
        val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
    }

}
