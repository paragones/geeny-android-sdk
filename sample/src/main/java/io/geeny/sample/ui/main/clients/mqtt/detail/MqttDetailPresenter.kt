package io.geeny.sample.ui.main.clients.mqtt.detail

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Scheduler

class MqttDetailPresenter(
        val serverUri: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<MqttDetailView>() {
    fun load() {
        add(
                sdk.clients.mqtt.get(serverUri)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showClient(it) },
                                view?.showError()
                        )
        )


        add(
                sdk.routing.router.list()
                        .flatMapIterable { it }
                        .filter { it.info().type == RouteType.MQTT }
                        .toList().toObservable()
                        .subscribe(
                                { view?.onListLoaded(it) },
                                view?.showError()
                        )
        )
    }

    fun createTopic(mqttTopic: String) {

        add(
                sdk.clients.mqtt.get(serverUri)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showMqttTopicForm(it, mqttTopic) },
                                view?.showError()
                        )
        )
    }

}

interface MqttDetailView : BaseView {
    fun showClient(mqttClient: GeenyMqttClient)
    fun onListLoaded(routes: List<Route>)
    fun showMqttTopicForm(client: GeenyMqttClient, mqttTopic: String)
}