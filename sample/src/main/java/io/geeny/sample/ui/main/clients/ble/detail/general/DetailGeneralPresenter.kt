package io.geeny.sample.ui.main.clients.ble.detail.general

import android.bluetooth.BluetoothGattService
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.Observable
import io.reactivex.Scheduler

class DetailGeneralPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<DetailGeneralView>() {
    var connection: BleClient? = null

    fun load() {
        add(
                sdk.clients.ble.getClient(address)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribe(
                                {
                                    view?.onConnectionLoaded(it)
                                    connection = it

                                    add(connection!!.services()
                                            .observeOn(mainScheduler)
                                            .subscribe {
                                                view?.onServiceLoaded(it)
                                                view?.progress(false)
                                            })
                                },
                                view?.showError()
                        )
        )
    }
}

interface DetailGeneralView : BaseView {
    fun onConnectionLoaded(connection: BleClient)
    fun onServiceLoaded(services: List<BluetoothGattService>)
}