package io.geeny.sdk.geeny.cloud

import android.content.Context
import io.geeny.sdk.BuildConfig
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.common.KeyValueStore
import io.geeny.sdk.common.ListDisk
import io.geeny.sdk.geeny.auth.AuthenticationComponent
import io.geeny.sdk.geeny.cloud.api.endpoints.MessageTypeEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingEndpoint
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingPostBody
import io.geeny.sdk.geeny.cloud.api.endpoints.ThingTypeEndpoint
import io.geeny.sdk.geeny.cloud.api.repos.*
import io.geeny.sdk.geeny.cloud.api.repos.certificate.CertificateRepository
import io.geeny.sdk.geeny.cloud.api.repos.messagetype.MessageTypeJsonConverter
import io.geeny.sdk.geeny.cloud.api.repos.messagetype.MessageTypeRepository
import io.geeny.sdk.geeny.cloud.api.repos.resource.ThingTypeJsonConverter
import io.geeny.sdk.geeny.cloud.api.repos.resource.ThingTypeRepository
import io.geeny.sdk.geeny.cloud.api.repos.thing.ThingJsonConverter
import io.geeny.sdk.geeny.cloud.api.repos.thing.ThingRepository
import io.reactivex.Observable
import io.reactivex.ObservableSource
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class CloudComponent(val configuration: GeenyConfiguration,
                     val auth: AuthenticationComponent,
                     val keyValueStore: KeyValueStore,
                     val context: Context) {

    private val authInterceptor: Interceptor = Interceptor { chain ->
        val originalRequest = chain?.request()!!

        val token = auth.token()

        val newRequest = originalRequest.newBuilder()
                .header("Authorization", "JWT $token")
                .build()

        chain.proceed(newRequest)
    }


    val client: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
                .addInterceptor(authInterceptor)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })
                .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(configuration.environment.thingApiBaseUrl())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val thingRepository: ThingRepository by lazy {
        ThingRepository(
                object : SimpleCache<Thing>(mutableMapOf()) {
                    override fun id(t: Thing): String = t.id
                },
                ListDisk(keyValueStore, ThingJsonConverter, "THING_LIST_ID"),
                retrofit.create(ThingEndpoint::class.java))
    }


    val thingTypeRepository: ThingTypeRepository by lazy {
        ThingTypeRepository(
                retrofit.create(ThingTypeEndpoint::class.java),
                object : SimpleCache<ThingType>() {
                    override fun id(t: ThingType): String = t.id
                },
                ListDisk(keyValueStore, ThingTypeJsonConverter, "THINGTYPE_LIST_ID")
        )
    }

    val messageTypeRepository: MessageTypeRepository by lazy {
        MessageTypeRepository(
                retrofit.create(MessageTypeEndpoint::class.java),
                object : SimpleCache<MessageType>() {
                    override fun id(t: MessageType): String = t.id
                },
                ListDisk(keyValueStore, MessageTypeJsonConverter, "MESSAGE_TYPE_LIST_ID")
        )
    }

    val certificateRepository: CertificateRepository by lazy {
        CertificateRepository(context!!)
    }


    fun onInit(sdkInitializationResult: SdkInitializationResult): ObservableSource<SdkInitializationResult> = Observable.just(sdkInitializationResult)
    fun onTearDown(result: SdkTearDownResult): Observable<SdkTearDownResult> = Observable.just(result)
    fun register(deviceInfo: DeviceInfo): Observable<Thing> =
            Observable.just(deviceInfo)
                    .map {
                        ThingPostBody(
                                it.deviceName,
                                it.serialNumber.toString(),
                                it.thingTypeId.toString()
                        )
                    }
                    .flatMap { thingRepository.create(it) }

}

