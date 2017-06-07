package io.geeny.sdk.routing.router.custom

import android.content.Context
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteProducer
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class CustomProducerRoute(val info: RouteInfo, broker: BoteBroker, val client: Client) : BoteProducer(broker, info.topic), Route {
    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    private val runningStream: Stream<ConnectionState> = Stream()


    override fun info(): RouteInfo = info

    override fun connection(): Observable<ConnectionState> = Observable.just(ConnectionState.CONNECTED)

    override fun running(): Observable<ConnectionState> = runningStream.connect()

    private var disposable: Disposable? = null

    init {
        runningStream.set(ConnectionState.DISCONNECTED)
    }

    override fun start(context: Context): Boolean {
        runningStream.set(ConnectionState.CONNECTED)
        disposable = client.value(info.clientResourceId)
                .subscribe {
                    GLog.d(TAG, "sending $it")
                    broker.send(info.topic, it)
                }
        client.notify(info.clientResourceId, true)
        return true
    }

    override fun stop(): Boolean {
        disposable?.dispose()
        runningStream.set(ConnectionState.DISCONNECTED)
        client.notify(info.clientResourceId, false)
        return true
    }

    companion object {
        private val TAG = CustomProducerRoute::class.java.simpleName
    }
}