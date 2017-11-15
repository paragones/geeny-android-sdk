package io.geeny.sdk

import android.content.Context
import io.geeny.sdk.clients.common.Client
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

class VirtualThingGateway(
        private val address: String,
        private val sdk: GeenySdk,
        private val mainScheduler: Scheduler,
        private val ioScheduler: Scheduler = Schedulers.io()) {

    private var client: Client? = null
    private var compositeDisposable: CompositeDisposable? = null
    var callback: Callback? = null
    private var clientInfo: LocalThingInfo? = null

    private fun add(disposable: Disposable) {
        compositeDisposable?.add(disposable)
    }

    fun attach(callback: Callback) {
        this.callback = callback

        compositeDisposable = CompositeDisposable()

        callback.progress(true)
        add(
                sdk.clients.custom.getClient(address)
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


    private fun onClientLoaded(client: Client) {
        this.client = client

        add(
                client.geenyInformation()
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    if (clientInfo == null) {
                                        GLog.d(TAG, "Geeny Device Information loaded $it")
                                        callback?.progress(false)
                                        loadThing(it)
                                        clientInfo = it

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

    private fun loadThing(clientInfo: LocalThingInfo) {
        add(
                sdk.geeny.getThing(clientInfo.serialNumber)
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
                sdk.geeny.getFlows(thing, RouteType.CUSTOM)
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
                            .observeOn(mainScheduler)
                            .subscribe {
                                GLog.d(TAG, "Value has changed: " + it.toHex(true))
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

    fun register() {
        if (clientInfo != null) {
            callback?.progress(true)
            add(
                    sdk.geeny.register(clientInfo!!)
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

    fun start(flow: GeenyFlow, context: Context) {
        flow.routes.forEach {
            it.start(context)
        }
    }

    companion object {
        val TAG = VirtualThingGateway::class.java.simpleName
    }


    interface Callback {
        fun onLocalThingInfoLoad(deviceInfo: LocalThingInfo)
        fun onDeviceIsNotRegisteredYet()
        fun onFlowsLoaded(flows: List<GeenyFlow>)
        fun onRouteConnectionStatusHasChanged(flow: GeenyFlow, route: Route, status: ConnectionState)
        fun progress(show: Boolean)
        fun onError(throwable: Throwable)
        fun onValueHasChanged(flow: GeenyFlow, bytes: ByteArray)
    }
}