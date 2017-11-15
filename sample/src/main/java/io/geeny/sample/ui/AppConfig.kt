package io.geeny.sample.ui

class AppConfig {

    companion object {
        val NORMAL_RESOURCE_ID = "resource.id.source.normal"
        val AVERAGE_RESOURCE_ID = "resource.id.channel.average"
        val TIMES2_PRODUCER_RESOURCE_ID = "resource.id.channel.times2"
        val DIVIDE_BY_10_CHANNEL_RESOURCE_ID = "resource.id.channel.divide10"
        val COUNT_PRODUCER_RESOURCE_ID = "resource.id.source.count"
        val EXP_COUNT_PRODUCER_RESOURCE_ID = "resource.id.source.count.exp"
        val RANDOM_PRODUCER_RESOURCE_ID = "resource.id.source.random"
        val APP_LOG_RESOURCE_ID = "resource.id.sink.log"
        val APP_TAG: String = "GATEWAY_APP"
        val DISCO_SOURCE_RESOURCE_ID: String = "resource.id.source.disco"
        val DANCE_SOURCE_RESOURCE_ID: String = "resource.id.source.dance"
    }
}