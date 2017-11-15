package io.geeny.sample.ui.main.clients.ble.detail.geeny

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.common.GLog
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.geeny.sdk.geeny.things.Thing
import io.geeny.sdk.geeny.things.emptyBleThing
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Observable
import io.reactivex.Scheduler

class DetailGeenyPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<DetailGeenyView>() {
    var connection: BleClient? = null

    fun load() {
        add(
                sdk.clients.ble.getClient(address)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { onConnectionLoaded(it) },
                                view?.showError()
                        )
        )
    }

    private var geenyInformation: LocalThingInfo? = null

    fun onConnectionLoaded(client: BleClient) {
        view?.onConnectionLoaded(client)


        add(
                client.geenyInformation()
                        .doOnNext { this.geenyInformation = it }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    view?.publishGeenyInformation(it)
                                    loadThing(it.serialNumber.toString())
                                },
                                view?.showError()
                        )
        )

    }

    private fun loadThing(id: String) {

        add(
                sdk.geeny.getThing(id)
                        .defaultIfEmpty(emptyBleThing)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    GLog.d(TAG, "Loaded device $it")
                                    if (it.localThingInfo.address.isEmpty()) {
                                        view?.onDeviceIsUnregistered()
                                    } else {
                                        onDeviceIsRegistered(it)
                                    }
                                },
                                view?.showError()
                        )
        )

    }

    private fun onDeviceIsRegistered(thing: Thing) {

        GLog.i(TAG, "device is registered $thing")
        add(
                sdk.geeny.getFlows(thing, RouteType.BLE)
                        .doOnSubscribe { GLog.d(TAG, "Subscribing to getFlow") }
                        .doOnError { GLog.d(TAG, it.message!!) }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.onFlowLoaded(it) },
                                view?.showError()
                        )
        )
    }

    fun register() {
        view?.progress(true)
        add(
                sdk.geeny.register(geenyInformation!!)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { onDeviceIsRegistered(it) },
                                view?.showError(),
                                {
                                    view?.progress(false)
                                }
                        )
        )
    }

    companion object {
        val TAG = DetailGeenyPresenter::class.java.simpleName
    }
}

interface DetailGeenyView : BaseView {
    fun onConnectionLoaded(connection: BleClient)
    fun publishGeenyInformation(deviceInfo: LocalThingInfo)
    fun onDeviceIsUnregistered()
    fun onFlowLoaded(flows: List<GeenyFlow>)
}