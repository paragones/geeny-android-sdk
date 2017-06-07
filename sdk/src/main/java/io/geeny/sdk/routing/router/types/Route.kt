package io.geeny.sdk.routing.router.types

import android.content.Context
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable

interface Route {
    fun info(): RouteInfo
    fun isEmpty(): Boolean = false
    fun identifier() = info().identifier()
    fun connection(): Observable<ConnectionState>
    fun running(): Observable<ConnectionState>
    fun start(context: Context): Boolean
    fun stop(): Boolean
    fun isStarted(): Boolean
}

class EmptyRoute(val type: RouteType = RouteType.EMPTY, val direction: Direction = Direction.CONSUMER) : Route {
    override fun connection(): Observable<ConnectionState> = Observable.error(IllegalStateException("This is function is not supported in an empty route"))
    override fun running(): Observable<ConnectionState> = Observable.error(IllegalStateException("This is function is not supported in an empty route"))
    override fun start(context: Context) = false
    override fun stop() = false
    override fun info(): RouteInfo = RouteInfo(type, direction, "", "", "")
    override fun isEmpty() = true
    override fun isStarted(): Boolean = false
}