package io.geeny.sdk

import android.content.Context
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.toHex 
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.geeny.sdk.geeny.things.Thing
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class BleGateway(
        private val bluetoothAdress: String,
        private val sdk: GeenySdk,
        private val mainScheduler: Scheduler,
        private val ioScheduler: Scheduler = Schedulers.io()) {

    private var client: BleClient? = null
    private var compositeDisposable: CompositeDisposable? = null
    var callback: Callback? = null
    private var deviceInfo: LocalThingInfo? = null

    private fun add(disposable: Disposable) {
        compositeDisposable?.add(disposable)
    }

    fun attach(callback: Callback) {
        this.callback = callback

        compositeDisposable = CompositeDisposable()

        callback.progress(true)
        add(
                sdk.clients.ble.getClient(bluetoothAdress)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { onClientLoaded(it) },
                                showError()
                        )
        )
    }

    fun detach() {
        this.callback = null
        compositeDisposable?.dispose()
    }


    private fun onClientLoaded(client: BleClient) {
        this.client = client
        callback?.onClientLoaded(client)

        add(
                client.connection()
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    GLog.d(TAG, "connection status has changed $it")
                                    callback?.onConnectionStateHasChanged(it)
                                },
                                showError()
                        )
        )

        add(
                client.geenyInformation()
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    if (deviceInfo == null) {
                                        GLog.d(TAG, "Geeny Device Information loaded $it")
                                        callback?.progress(false)
                                        loadThing(it)
                                        deviceInfo = it

                                        callback?.progress(false)
                                        callback?.onLocalThingInfoLoad(it)
                                    }
                                },
                                showError()
                        ))
    }

    private fun showError(): (Throwable) -> Unit = {
        callback?.onError(it)
    }

    private fun loadThing(deviceInfo: LocalThingInfo) {
        add(
                sdk.geeny.getThing(deviceInfo.serialNumber.toString())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    GLog.d(TAG, "Loaded cloudThingInfo $it")
                                    if (it.cloudThingInfo.isEmpty) {
                                        callback?.onDeviceIsNotRegisteredYet()
                                    } else {
                                        loadFlows(it)
                                    }
                                },
                                showError()
                        )
        )
    }

    private fun loadFlows(thing: Thing) {
        add(
                sdk.geeny.getFlows(thing, RouteType.BLE)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    GLog.d(TAG, "Flows loaded $it")
                                    callback?.onFlowsLoaded(it)
                                    it.forEach { connectFlow(it) }
                                },
                                showError()
                        )
        )
    }

    private fun connectFlow(flow: GeenyFlow) {
        val resource = flow.startingResourceId()
        if (resource != null) {
            add(
                    client!!.value(resource)
                            .map { it.currentValue }
                            .observeOn(mainScheduler)
                            .subscribe {
                                GLog.d(TAG, "Value has changed: " + it.toHex())
                                callback?.onValueHasChanged(flow, it)
                            }
            )
        }

        flow.routes.forEach { route ->
            add(
                    route.running()
                            .observeOn(mainScheduler)
                            .subscribe {
                                callback?.onRouteConnectionStatusHasChanged(flow, route, it)
                            }
            )
        }
    }

    fun disconnect() {
        client?.disconnect()
    }

    fun connect(context: Context) {
        client?.connect(context)
    }

    fun register() {
        if (deviceInfo != null) {

            callback?.progress(true)
            add(
                    sdk.geeny.register(deviceInfo!!)
                            .subscribeOn(ioScheduler)
                            .observeOn(mainScheduler)
                            .subscribe(
                                    {
                                        callback?.progress(false)
                                        GLog.d(TAG, "Geeny Device registered $it")
                                        loadFlows(it)
                                    },
                                    showError()
                            )
            )
        }
    }

    fun triggerRead(resourceId: String) {
        val c = client?.characteristicById(resourceId)
        if (c != null) {
            client?.read(c)
        }
    }

    fun start(flow: GeenyFlow, context: Context) {
        flow.routes.forEach {
            it.start(context)
        }
    }

    companion object {
        val TAG = BleGateway::class.java.simpleName
    }


    interface Callback {
        fun onClientLoaded(client: BleClient)
        fun onConnectionStateHasChanged(connectionState: ConnectionState)
        fun onLocalThingInfoLoad(deviceInfo: LocalThingInfo)
        fun onDeviceIsNotRegisteredYet()
        fun onFlowsLoaded(flows: List<GeenyFlow>)
        fun onRouteConnectionStatusHasChanged(flow: GeenyFlow, route: Route, status: ConnectionState)
        fun progress(show: Boolean)
        fun onError(throwable: Throwable)
        fun onValueHasChanged(flow: GeenyFlow, bytes: ByteArray)
    }
}