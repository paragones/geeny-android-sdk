package io.geeny.sdk

import android.content.Context
import io.geeny.sdk.clients.ClientsComponent
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.DefaultKeyValueStore
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.geeny.GeenyComponent
import io.geeny.sdk.routing.RoutingComponent
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security


class GeenySdk private constructor(private val configuration: GeenyConfiguration, context: Context) {

    private val appContext: Context = context.applicationContext

    val geeny: GeenyComponent by lazy {
        GeenyComponent(configuration, keyValueStore, context, clients.mqtt, routing.router)
    }

    val routing: RoutingComponent by lazy {
        RoutingComponent(configuration, clients.mqtt.pool, clients.ble.pool, clients.custom.pool, keyValueStore)
    }

    val clients: ClientsComponent by lazy {
        ClientsComponent(configuration, keyValueStore, appContext)
    }

    val keyValueStore: KeyValueStore by lazy {
        DefaultKeyValueStore(appContext)
    }

    fun getBleGateway(bluetoothAdress: String, mainScheduler: Scheduler): BleGateway {
        return BleGateway(bluetoothAdress, this, mainScheduler)
    }

    init {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    fun init(): Observable<SdkInitializationResult> {
        return Observable.just(SdkInitializationResult())
                .doOnNext { GLog.d(TAG, "Starting initialization") }
                .flatMap { clients.onInit(it) }
                .doOnNext { GLog.d(TAG, "Clients initialized") }
                .flatMap { routing.onInit(it) }
                .doOnNext { GLog.d(TAG, "Routing initialized") }
                .flatMap { geeny.onInit(it) }
    }

    fun tearDown(): Observable<SdkTearDownResult> {
        return Observable.just(SdkTearDownResult())
                .flatMap { geeny.onTearDown(it) }
                .flatMap { routing.onTearDown(it) }
                .flatMap { clients.onTearDown(it) }
    }

    companion object {

        val TAG = GeenySdk::class.java.simpleName
        private var sdkInstance: GeenySdk? = null

        fun create(configuration: GeenyConfiguration, context: Context): GeenySdk {
            if (sdkInstance != null) {
                throw IllegalStateException("Only one instance of the GeenySdk is allowed!")
            }
            sdkInstance = GeenySdk(configuration, context)
            return sdkInstance!!
        }
    }
}

data class SdkInitializationResult(
        var numberOfRoutesLoaded: Int = 0,
        var topicsLoaded: Int = 0,
        var mqttConfig: MutableList<MqttConfig> = ArrayList(),
        var isSignedIn: Boolean = false,
        var isCertificateLoaded: Boolean = false
) {
    fun isSuccess(): Boolean {
        return true
    }

    fun addMqttConfig(config: MqttConfig) {
        mqttConfig.add(config)
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("#routes loaded: ").append(numberOfRoutesLoaded).append("\n")
        sb.append("#topics loaded: ").append(topicsLoaded).append("\n")
        for (mqttConfig in mqttConfig) {
            sb.append("GeenyMqttClient created " + mqttConfig)
        }

        return sb.toString()
    }
}


class SdkTearDownResult() {
    fun isSuccess(): Boolean {
        return true
    }
}
