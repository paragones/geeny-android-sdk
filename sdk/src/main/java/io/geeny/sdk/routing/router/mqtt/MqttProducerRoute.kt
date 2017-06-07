package io.geeny.sdk.routing.router.mqtt

import android.content.Context
import io.geeny.sdk.clients.common.Stream
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.clients.mqtt.MqttMessageWrapper
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.TypeConverters
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.BoteProducer
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets


class MqttProducerRoute(
        val info: RouteInfo,
        broker: BoteBroker,
        val mqttClient: GeenyMqttClient) : BoteProducer(broker, info.topic), Route {

    override fun isStarted(): Boolean = runningStream.value == ConnectionState.CONNECTED
    private val runningStream: Stream<ConnectionState> = Stream()
    override fun running(): Observable<ConnectionState> = runningStream.connect()

    init {
        runningStream.set(ConnectionState.DISCONNECTED)
        // connectSecure to mqtt connection info
        mqttClient.connectionStream.connect()
                .subscribe { onConnectionStatusChanged(it) }
        // connectSecure to received connection
        mqttClient.publisherReceived
                .filter { it.topic == topic }
                .subscribe { onMessageArrived(it) }
    }


    private var isWaitingForConnection: Boolean = false


    override fun start(context: Context): Boolean {

        runningStream.set(ConnectionState.CONNECTING)

        GLog.d(TAG, "starting mqtt producer")

        when (mqttClient.connectionStream.value) {

            ConnectionState.CONNECTED -> {

                GLog.d(TAG, "Mqtt is connected, start producer route")
                startLoop()
                runningStream.set(ConnectionState.CONNECTED)
            }
            ConnectionState.DISCONNECTED -> {
                GLog.d(TAG, "Trying to connect to mqtt broker")
                isWaitingForConnection = true
                mqttClient.connect()
            }
            ConnectionState.CONNECTING -> {
                GLog.d(TAG, "Mqtt is connecting")
                isWaitingForConnection = true
            }
        }
        return true
    }


    private fun startLoop() {

        GLog.d(TAG, "Starting Mqtt Producer Loop")
        mqttClient.mqttSubscribe(info.topic, 0)
                .subscribe(
                        { GLog.d(TAG, it) },
                        { GLog.d(TAG, it.message.toString()) })

    }

    override fun stop(): Boolean {
        GLog.d(TAG, "Stop called")
        stopLoop()
        runningStream.set(ConnectionState.DISCONNECTED)
        return true
    }

    private fun stopLoop() {
        GLog.d(TAG, "Stopping Mqtt Producer Loop")
        mqttClient.mqttUnSubscribe(info.topic)
                .subscribe(
                        { GLog.d(TAG, it) },
                        { GLog.d(TAG, it.message.toString()) })

    }

    override fun connection(): Observable<ConnectionState> = mqttClient.connectionStream.connect()
    override fun info(): RouteInfo = info


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

                if(isWaitingForConnection) {
                    isWaitingForConnection = false
                    runningStream.set(ConnectionState.DISCONNECTED)
                }
            }
            ConnectionState.CONNECTING -> {
            }
        }
    }

    private fun onMessageArrived(message: MqttMessageWrapper) {
        val m = java.lang.String(message.message.payload, "UTF-8")
        val cm: kotlin.String = m.toString()
        val result = Integer.parseInt(cm)
        val format = TypeConverters.intToBytes(result, ByteOrder.BIG_ENDIAN)
        broker.send(info.topic, format)
    }

    override fun identifier(): String = info.identifier()

    companion object {
        val TAG = MqttProducerRoute::class.java.simpleName
    }
}
 