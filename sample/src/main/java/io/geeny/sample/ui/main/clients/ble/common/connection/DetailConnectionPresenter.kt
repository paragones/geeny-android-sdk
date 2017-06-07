package io.geeny.sample.ui.main.clients.ble.common.connection

import android.content.Context
import android.util.Log
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sample.ui.main.clients.ble.detail.BleClientPresenter
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.Observable
import io.reactivex.Scheduler

class DetailConnectionPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<DetailConnectionView>() {
    var connection: BleClient? = null

    fun load() {
        add(
                sdk.clients.ble.getClient(address)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribe(
                                {
                                    view?.onConnectionLoaded(it)
                                    connection = it

                                    add(connection!!.connection()
                                            .observeOn(mainScheduler)
                                            .subscribe {
                                                Log.d(BleClientPresenter.TAG, "" + it)
                                                view?.onConnectionStatusHasChanged(it)
                                            })
                                },
                                view?.showError()
                        )
        )
    }


    fun connectOrDisconnect(context: Context) {
        when (connection?.connectionStatus()) {
            ConnectionState.CONNECTED -> {
                connection?.disconnect()
            }
            ConnectionState.DISCONNECTED -> {
                connection?.connect(context)
            }
            ConnectionState.CONNECTING -> {
            }
        }
    }
}

interface DetailConnectionView : BaseView {
    fun onConnectionLoaded(connection: BleClient)
    fun onConnectionStatusHasChanged(connectionState: ConnectionState)
}