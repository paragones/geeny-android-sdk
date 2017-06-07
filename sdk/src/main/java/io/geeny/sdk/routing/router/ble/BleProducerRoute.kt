package io.geeny.sdk.routing.router.ble

import android.content.Context
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteProducer
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class BleProducerRoute(
        val info: RouteInfo,
        val client: BleClient,
        broker: BoteBroker) : BoteProducer(broker, info.topic), Route {
    private val runningStream: Stream<ConnectionState> = Stream()
    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    override fun running(): Observable<ConnectionState> = runningStream.connect()
    override fun connection(): Observable<ConnectionState> = client.connection()

    private var isWaitingForConnection = false
    private var isWaitingForService = false

    init {
        client.value(info.clientResourceId)
                .subscribe {
                    info("Value arrived" + it.formattedValue(), identifier())
                    send(it.currentValue)
                }
        runningStream.set(ConnectionState.DISCONNECTED)
        client.connection()
                .subscribe {
                    when (it) {
                        ConnectionState.CONNECTED -> {
                            if (isWaitingForConnection) {
                                isWaitingForConnection = false
                                if (client.hasServiceLoaded()) {
                                    connectResource()
                                } else {
                                    waitForService()
                                }
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            // TODO think about auto connect strategies
                            if(isStarted()) {
                                isWaitingForConnection = true
                                stop()
                            }
                        }
                        ConnectionState.CONNECTING -> {

                        }
                    }
                }

        client.services()
                .subscribe {
                    if (isWaitingForService && it.isNotEmpty()) {
                        isWaitingForService = false
                        connectResource()
                    }
                }
    }

    private fun connectResource() {
        client.notify(info.clientResourceId, true)
        runningStream.set(ConnectionState.CONNECTED)
    }

    fun waitForService() {
        runningStream.set(ConnectionState.CONNECTING)
        isWaitingForService = true
    }

    fun waitForConnection(context: Context) {
        isWaitingForConnection = true
        client.connect(context)
        runningStream.set(ConnectionState.CONNECTING)
    }

    override fun start(context: Context): Boolean {
        runningStream.set(ConnectionState.CONNECTING)

        if (!client.isConnected()) {
            waitForConnection(context)
            return false
        }

        if (!client.hasServiceLoaded()) {
            waitForService()
        } else {
            connectResource()
        }

        return true
    }

    override fun stop(): Boolean {
        isWaitingForConnection = false
        isWaitingForService = false

        if (client.hasServiceLoaded()) {
            client.notify(info.clientResourceId, false)
            runningStream.set(ConnectionState.DISCONNECTED)
        }

        // if service is not loaded this has never started anyhow
        return false
    }

    override fun info(): RouteInfo = info
    override fun identifier(): String = info.identifier()

    companion object {
        fun info(msg: String, id: String) {
            GLog.i("BleProducerRoute", "$id - $msg")
        }

        val TAG = BleProducerRoute::class.java.simpleName
    }
}