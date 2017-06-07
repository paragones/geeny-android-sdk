package io.geeny.sdk.mqtt

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.github.daemontus.unwrap
import io.geeny.sdk.geeny.cloud.CloudComponent
import io.geeny.sdk.geeny.cloud.api.repos.Certificate
import io.geeny.sdk.clients.mqtt.certificate.sslSocketFactoryFrom
import junit.framework.Assert.assertEquals
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.junit.Test
import org.junit.runner.RunWith
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security


@RunWith(AndroidJUnit4::class)
class MqttTest {


    companion object {
        val test_filename = "TEST_FILENAME"
    }

    init {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }


    @Test
    fun testSetup() {

        val appContext = InstrumentationRegistry.getTargetContext()
        val cm = CloudComponent("http://10.0.2.2:8080", appContext)
        val repos = cm.certificateRepository

        var certificate: Certificate

        repos
                .downloadCertificate("someid")
                .flatMap { repos.save(it, test_filename) }
                .subscribe {
                    certificate = it
                }
    }

    @Test
    fun testMqttConnection() {

        val appContext = InstrumentationRegistry.getTargetContext()
        val cm = CloudComponent("http://10.0.2.2:8080", appContext)
        val repos = cm.certificateRepository

        var certificate: Certificate = Certificate("", "", "")

        repos
                .load(test_filename)
                .subscribe {
                    certificate = it
                }

        val dir = appContext.filesDir.path.toString()
        val broker = "ssl://10.0.2.2:8883"
        val client = MqttClient(broker, MqttClient.generateClientId(), MqttDefaultFilePersistence(dir + "/mqttdir"))


        val options = MqttConnectOptions()
        options.socketFactory = sslSocketFactoryFrom(io.geeny.sdk.clients.mqtt.certificate.to(certificate).unwrap(), "password")

        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun connectionLost(cause: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        client.connect(options)


        Thread.sleep(10000)
        assertEquals("yeah", "yeah")

    }
}