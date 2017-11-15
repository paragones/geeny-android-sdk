package io.geeny.sdk.clients.mqtt

import android.content.Context
import android.util.Log
import com.github.daemontus.unwrap
import io.geeny.sdk.clients.common.*
import io.geeny.sdk.clients.mqtt.certificate.sslSocketFactoryFrom
import io.geeny.sdk.clients.mqtt.certificate.toCertificateInfo
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.TypeConverters
import io.geeny.sdk.geeny.cloud.api.repos.CertificatesInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.PrintWriter
import java.io.StringWriter
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.UnsupportedEncodingException


data class MqttMessageWrapper(val topic: String, val message: MqttMessage)

class GeenyMqttClient(val mqttConfig: MqttConfig, context: Context) {

    val connectionStream: Stream<ConnectionState> = Stream()
    val client: MqttAndroidClient

    val publisherReceived: PublishSubject<MqttMessageWrapper> = PublishSubject.create()
    val publisherSent: PublishSubject<MqttMessageWrapper> = PublishSubject.create()

    var certificateInfo: CertificatesInfo? = null

    fun connection(): Observable<ConnectionState> = connectionStream.connect()

    init {
        if (mqttConfig.isSecure) {
            certificateInfo = toCertificateInfo(mqttConfig.certificate!!).unwrap()
        }

        connectionStream.set(ConnectionState.DISCONNECTED)
        client = MqttAndroidClient(context, mqttConfig.serverUri, mqttConfig.clientId)
        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                GLog.d(TAG, "message arrived: " + message)
                publisherReceived.onNext(MqttMessageWrapper(topic!!, message!!))
            }

            override fun connectionLost(cause: Throwable?) {
                GLog.d(TAG, "connectionLost: " + cause?.message)
                connectionStream.set(ConnectionState.DISCONNECTED)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                GLog.d(TAG, "deliveryComplete: " + token?.message)
                publisherSent.onNext(MqttMessageWrapper("", token!!.message))
            }
        })
    }


    fun connect() {
        GLog.d(TAG, "Trying to connect securely to $mqttConfig")
        connectionStream.set(ConnectionState.CONNECTING)

        val options =
                if (mqttConfig.isSecure) {
                    MqttConnectOptions().apply {
                        socketFactory = sslSocketFactoryFrom(certificateInfo!!, "password")
                    }
                } else {
                    MqttConnectOptions().apply {}
                }

        client.connect(options).actionCallback =
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        GLog.d(TAG, "Connection succeeded")
                        connectionStream.set(ConnectionState.CONNECTED)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        val errors = StringWriter()
                        exception.printStackTrace(PrintWriter(errors))
                        GLog.d(TAG, "Failed to connect to insecure server...." + exception.message)
                        connectionStream.set(ConnectionState.DISCONNECTED)
                    }
                }
    }

    fun subscribe(topic: String): Observable<MqttMessageWrapper> {
        client.subscribe(topic, 1).actionCallback =
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        GLog.d(TAG, "success subscribing to $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.d(TAG, "success subscribing failed ${exception.message}")
                    }

                }

        return publisherReceived.filter({ it.topic == topic })
    }


    fun mqttSubscribe(topic: String, qos: Int): Single<String> {
        return Single.create { subscriber ->
            client.subscribe(topic, qos).actionCallback =
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            GLog.d(TAG, "success subscribing to $topic")
                            subscriber.onSuccess(topic)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            GLog.d(TAG, "success subscribing failed ${exception.message}")
                            subscriber.onError(exception)
                        }
                    }
        }
    }

    fun mqttUnSubscribe(topic: String): Single<String> {
        return Single.create { subscriber ->
            client.unsubscribe(topic).actionCallback =
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            GLog.d(TAG, "success subscribing to $topic")
                            subscriber.onSuccess(topic)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            GLog.d(TAG, "success subscribing failed ${exception.message}")
                            subscriber.onError(exception)
                        }
                    }
        }
    }

    fun send(topic: String, message: String) {
        try {
            send(topic, message.toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun send(topic: String, message: ByteArray) {
        try {
            val result = TypeConverters.bytesToIntDynamic(message)
            GLog.d(TAG, "Sending message over $topic $result")
            val message = MqttMessage(result.toString().toByteArray())
            message.isRetained = false
            client.publish(topic.trim(), message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        client.disconnect()
    }

    companion object {
        val TAG = GeenyMqttClient::class.java.simpleName
    }

    fun isSecure(): Boolean = mqttConfig.isSecure
}
