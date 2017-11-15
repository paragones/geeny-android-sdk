package com.leondroid.demo_app.ui.main.virtualthings

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.common.Client
import io.reactivex.Scheduler
import java.util.*

class VirtualThingsPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<VirtualThingsView>() {

    fun load() {
        sdk.clients.custom.availableDevices()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    view?.onThingsLoaded(it)
                }

    }
}


interface VirtualThingsView : BaseView {
    fun onThingsLoaded(customClients: List<Client>)
}