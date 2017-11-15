package io.geeny.sdk.routing.router.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.TypeConverters
import io.geeny.sdk.common.toHex
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteConsumer
import io.geeny.sdk.routing.bote.topicjournal.BoteResponse
import io.geeny.sdk.routing.bote.topicjournal.MessageType
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable

class BleConsumerRoute(
        val info: RouteInfo,
        val client: BleClient,
        broker: BoteBroker) :
        BoteConsumer(broker, info.topic, delay = 500), Route {
    private val runningStream: Stream<ConnectionState> = Stream()
    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    override fun running(): Observable<ConnectionState> = runningStream.connect()
    override fun connection(): Observable<ConnectionState> = client.connection()

    private var isWaitingForConnection = false
    private var isWaitingForService = false

    private var characteristic: BluetoothGattCharacteristic? = null

    init {
        runningStream.set(ConnectionState.DISCONNECTED)
        client.connection()
                .subscribe {
                    when (it) {
                        ConnectionState.CONNECTED -> {
                            if (isWaitingForConnection) {
                                isWaitingForConnection = false
                                if (client.hasServiceLoaded()) {
                                    prepareLoop()
                                } else {
                                    waitForService()
                                }
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            // TODO think about auto connect strategies
                            if (isStarted()) {
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
                        prepareLoop()
                    }
                }
    }

    private fun waitForService() {
        runningStream.set(ConnectionState.CONNECTING)
        isWaitingForService = true
    }

    private fun waitForConnection(context: Context) {
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
            prepareLoop()
        }

        return true
    }

    override fun stop(): Boolean {
        isWaitingForConnection = false
        isWaitingForService = false
        stopLoop()
        runningStream.set(ConnectionState.DISCONNECTED)
        return true
    }

    private fun prepareLoop() {
        if (characteristic == null) {
            characteristic = client.characteristicById(info.clientResourceId)
        }

        runningStream.set(ConnectionState.CONNECTED)
        startLoop()
    }

    override fun info(): RouteInfo = info

    override fun onResponse(response: BoteResponse) {
        if (response.messageType != MessageType.READ) {
            return
        }
        val payload = response.payload
        val res = TypeConverters.bytesToIntDynamic(payload!!)
        val responsePayload = TypeConverters.intToBytesDynamic(res)

        GLog.d(TAG, responsePayload.toHex(true))
        client.write(characteristic!!, responsePayload)
    }

    override fun onError(t: Throwable) {}

    companion object {
        val TAG = BleConsumerRoute::class.java.simpleName
    }
}