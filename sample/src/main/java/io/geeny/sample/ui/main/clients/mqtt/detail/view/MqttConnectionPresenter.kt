package io.geeny.sample.ui.main.clients.ble.common.connection

import android.content.Context
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.reactivex.Observable
import io.reactivex.Scheduler

class MqttConnectionPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<MqttConnectionView>() {
    var mqttClient: GeenyMqttClient? = null

    fun load() {
        add(
                sdk.clients.mqtt.get(address)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribe(
                                {
                                    view?.onConnectionLoaded(it)
                                    mqttClient = it

                                    add(mqttClient!!.connection()
                                            .observeOn(mainScheduler)
                                            .subscribe {
                                                view?.onConnectionStatusHasChanged(it)
                                            })
                                },
                                view?.showError()
                        )
        )
    }


    fun connectOrDisconnect(context: Context) {
        when (mqttClient?.connectionStream?.value) {
            ConnectionState.CONNECTED -> {
                mqttClient?.disconnect()
            }
            ConnectionState.DISCONNECTED -> {

                if(mqttClient == null) {
                    return
                }
                mqttClient!!.connect()
            }
            ConnectionState.CONNECTING -> {
            }
        }
    }
}

interface MqttConnectionView : BaseView {
    fun onConnectionLoaded(connection: GeenyMqttClient)
    fun onConnectionStatusHasChanged(connectionState: ConnectionState)
}