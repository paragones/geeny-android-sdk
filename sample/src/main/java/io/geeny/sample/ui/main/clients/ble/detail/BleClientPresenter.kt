package io.geeny.sample.ui.main.clients.ble.detail

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.Observable
import io.reactivex.Scheduler

class BleClientPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<BleConnectionView>() {

    var connection: BleClient? = null

    fun load() {

        add(
                sdk.clients.ble.getClient(address)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribe(
                                {view?.onConnectionLoaded(it)},
                                view?.showError()
                        )
        )
    }

    override fun detach() {
        connection = null
        super.detach()
    }

    companion object {
        val TAG = BleClientPresenter::class.java.simpleName
    }
}

interface BleConnectionView : BaseView {
    fun onConnectionLoaded(connection: BleClient)
}