package com.leondroid.demo_app

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ApplicationComponent(context: Context) {


    val sdk: GeenySdk by lazy {

        val config =
                GeenyConfiguration.Builder()
                        .withClientSecret("")
                        .withClientId("")
                        .build()

        GeenySdk.create(config, context)
    }

    val ioScheduler: Scheduler by lazy {
        Schedulers.io()
    }

    val mainScheduler: Scheduler by lazy {
        AndroidSchedulers.mainThread()
    }
}