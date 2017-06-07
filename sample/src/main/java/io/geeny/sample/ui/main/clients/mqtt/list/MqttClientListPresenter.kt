package io.geeny.sample.ui.main.clients.mqtt.list

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.reactivex.Scheduler

class MqttClientListPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<MqttClientListView>() {


    fun load() {
        add(
                sdk.clients.mqtt.list()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.mqttClientsLoaded(it) },
                                view?.showError()
                        )
        )
    }

    fun onMqttClientSelected(config: MqttConfig) {
        view?.openMqttClient(config)
    }
}

interface MqttClientListView : BaseView {
    fun mqttClientsLoaded(mqttClients: List<GeenyMqttClient>)
    fun openMqttClient(config: MqttConfig)
}