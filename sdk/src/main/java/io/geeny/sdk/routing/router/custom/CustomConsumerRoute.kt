package io.geeny.sdk.routing.router.custom

import android.content.Context
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteConsumer
import io.geeny.sdk.routing.bote.topicjournal.BoteResponse
import io.geeny.sdk.routing.bote.topicjournal.MessageType
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable

class CustomConsumerRoute(val info: RouteInfo, broker: BoteBroker, val client: Client) : BoteConsumer(broker, info.topic), Route {
    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    private val runningStream: Stream<ConnectionState> = Stream()

    override fun info(): RouteInfo = info

    override fun connection(): Observable<ConnectionState> = Observable.just(ConnectionState.CONNECTED)

    override fun running(): Observable<ConnectionState> = runningStream.connect()

    init {
        runningStream.set(ConnectionState.DISCONNECTED)
    }

    override fun start(context: Context): Boolean {
        GLog.i(TAG, "starting consumer route ${info()}")
        startLoop()
        runningStream.set(ConnectionState.CONNECTED)
        client.notify(info.clientResourceId, true)
        return true
    }

    override fun stop(): Boolean {
        stopLoop()
        runningStream.set(ConnectionState.DISCONNECTED)
        client.notify(info.clientResourceId, false)
        GLog.i(TAG, "stopping consumer route ${info()}")
        return true
    }

    override fun onResponse(response: BoteResponse) {
        if (response.messageType == MessageType.READ) {
            GLog.d(TAG, "Topic $topic consumed $response")
            client.write(ClientMessage(info.clientResourceId, response.payload!!)).subscribe()
        }
    }

    override fun onError(t: Throwable) {
        GLog.e(TAG, t.message, t)
    }

    companion object {
        val TAG = CustomConsumerRoute::class.java.simpleName
    }
}