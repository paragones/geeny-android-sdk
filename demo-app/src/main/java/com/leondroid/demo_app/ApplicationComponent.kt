package com.leondroid.demo_app

import android.content.Context
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.custom.VirtualThing
import io.geeny.sdk.clients.custom.slots.sink.LogSink
import io.geeny.sdk.clients.custom.slots.source.NormalDistributionSource
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.environment.Environment
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ApplicationComponent(context: Context) {


    val sdk: GeenySdk by lazy {


        val config =
                GeenyConfiguration.Builder()
                        .withEnvironment(Environment.Type.PRODUCTION)
                        .withVirtualThing(
                                VirtualThing.Builder("Example Virtual", "d63f6ad9-1cd0-4288-89cb-0dfe07bfead9", "fake_id")
                                        .withSlot(NormalDistributionSource("Normal Distribution", "0000cafe-c001-de30-cabb-785feabcd123", 0, 200, 1000))
                                        .withSlot(LogSink("receveiver", "0000da7a-c001-de30-cabb-785feabcd123", "EXAMPLE_LOG"))
                                        .build()
                        )
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

