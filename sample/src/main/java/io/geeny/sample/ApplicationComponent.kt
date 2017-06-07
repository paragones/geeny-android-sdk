package io.geeny.sample

import android.content.Context
import io.geeny.sample.ui.AppConfig
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.clients.custom.slots.channel.DividedByChannel
import io.geeny.sdk.clients.custom.slots.sink.LogSink
import io.geeny.sdk.clients.custom.slots.channel.IntegerAverageChannel
import io.geeny.sdk.clients.custom.slots.channel.TimesChannel
import io.geeny.sdk.clients.custom.slots.source.DiscoSource
import io.geeny.sdk.clients.custom.slots.source.FunctionalSource
import io.geeny.sdk.clients.custom.slots.source.NormalDistributionSource
import io.geeny.sdk.clients.custom.slots.source.RandomSource
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class ApplicationComponent(val context: Context) {


    val sdk: GeenySdk by lazy {

        val config =
                GeenyConfiguration.Builder()
                        .withClientId("")
                        .withClientSecret("")
                        .withMqtt(MqttConfig("tcp://broker.hivemq.com:1883", "Public Broker", false))
                        .withSlot(AppConfig.NORMAL_RESOURCE_ID, NormalDistributionSource("Normal Distribution (420, 1140)", AppConfig.NORMAL_RESOURCE_ID, 420, 1140, 20))
                        .withSlot(AppConfig.AVERAGE_RESOURCE_ID, IntegerAverageChannel("Averaging", AppConfig.AVERAGE_RESOURCE_ID, 10))
                        .withSlot(AppConfig.TIMES2_PRODUCER_RESOURCE_ID, TimesChannel(AppConfig.TIMES2_PRODUCER_RESOURCE_ID, 1000))
                        .withSlot(AppConfig.DIVIDE_BY_10_CHANNEL_RESOURCE_ID, DividedByChannel(AppConfig.DIVIDE_BY_10_CHANNEL_RESOURCE_ID, 1000))
                        .withSlot(AppConfig.DISCO_SOURCE_RESOURCE_ID, DiscoSource(AppConfig.DISCO_SOURCE_RESOURCE_ID, 1000))
                        .withSlot(AppConfig.COUNT_PRODUCER_RESOURCE_ID, FunctionalSource("Count Example", AppConfig.COUNT_PRODUCER_RESOURCE_ID, { it }, 3000))
                        .withSlot(AppConfig.EXP_COUNT_PRODUCER_RESOURCE_ID, FunctionalSource("Exp", AppConfig.EXP_COUNT_PRODUCER_RESOURCE_ID, { Math.exp(it.toDouble() / 100).toInt() }, 10))
                        .withSlot(AppConfig.APP_LOG_RESOURCE_ID, LogSink("Logger", AppConfig.APP_LOG_RESOURCE_ID, AppConfig.APP_TAG))
                        .withSlot(AppConfig.RANDOM_PRODUCER_RESOURCE_ID, RandomSource("Random Example", AppConfig.RANDOM_PRODUCER_RESOURCE_ID))
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
 