package io.geeny.sdk.routing.router.mqtt

import android.content.Context
import io.geeny.sdk.clients.common.ClientMessage
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteConsumer
import io.geeny.sdk.routing.bote.BoteProducer
import io.geeny.sdk.routing.bote.topicjournal.BoteResponse
import io.geeny.sdk.routing.bote.topicjournal.MessageType
import io.geeny.sdk.routing.router.custom.CustomConsumerRoute
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable


class MqttConsumerRoute(
        val info: RouteInfo,
        broker: BoteBroker,
        val mqttClient: GeenyMqttClient) : BoteConsumer(broker, info.topic), Route {

    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    private val runningStream: Stream<ConnectionState> = Stream()
    override fun running(): Observable<ConnectionState> = runningStream.connect()
    private var isWaitingForConnection: Boolean = false

    init {
        runningStream.set(ConnectionState.DISCONNECTED)
        // connectSecure to mqtt connection info
        mqttClient.connectionStream.connect()
                .subscribe { onConnectionStatusChanged(it) }
    }

    private fun onConnectionStatusChanged(connectionState: ConnectionState) {
        when (connectionState) {
            ConnectionState.CONNECTED -> {
                if (isWaitingForConnection) {
                    startLoop()
                    runningStream.set(ConnectionState.CONNECTED)
                    isWaitingForConnection = false
                }
            }
            ConnectionState.DISCONNECTED -> {
                if (isStarted()) {
                    stopLoop()
                }

                if (isWaitingForConnection) {
                    isWaitingForConnection = false
                    runningStream.set(ConnectionState.DISCONNECTED)
                }
            }
            ConnectionState.CONNECTING -> {
            }
        }
    }

    override fun connection(): Observable<ConnectionState> = mqttClient.connectionStream.connect()

    override fun start(context: Context): Boolean {

        runningStream.set(ConnectionState.CONNECTING)

        GLog.d(BoteProducer.TAG, "starting mqtt producer")

        when (mqttClient.connectionStream.value) {

            ConnectionState.CONNECTED -> {

                GLog.d(BoteProducer.TAG, "Mqtt is connected, start producer route")
                startLoop()
                runningStream.set(ConnectionState.CONNECTED)
            }
            ConnectionState.DISCONNECTED -> {
                GLog.d(BoteProducer.TAG, "Trying to connect to mqtt broker")
                isWaitingForConnection = true
                mqttClient.connect()
            }
            ConnectionState.CONNECTING -> {
                GLog.d(BoteProducer.TAG, "Mqtt is connecting")
                isWaitingForConnection = true
            }
        }
        return true
    }

    override fun stop(): Boolean {
        stopLoop()
        runningStream.set(ConnectionState.DISCONNECTED)
        return true
    }

    override fun info(): RouteInfo = info

    override fun onResponse(response: BoteResponse) {
        if (response.messageType == MessageType.READ) {
            GLog.d(TAG, "MqttConsumed: " + response)
            mqttClient.send(info.clientResourceId, response.payload!!)
        }
    }

    override fun onError(t: Throwable) {

    }

    override fun identifier(): String = info.identifier()

    companion object {
        val TAG = MqttConsumerRoute::class.java.simpleName
    }
}
 