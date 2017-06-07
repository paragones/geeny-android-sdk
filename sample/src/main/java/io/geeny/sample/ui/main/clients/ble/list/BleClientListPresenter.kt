package io.geeny.sample.ui.main.clients.ble.list

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.Scheduler
import java.util.*
import kotlin.Comparator

class BleConnectionListPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<BleClientListView>() {


    override fun attach(view: BleClientListView) {
        super.attach(view)

        add(
                sdk.clients.ble.availableDevices()
                        .map {
                            Collections.sort(it, DeviceComparable())
                            it
                        }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view.onListUpdated(it) }
                        ))
    }

    override fun detach() {
        stopScanning()
        this.view = null
    }

    fun scanBleDevices() {
        view?.showProgress(true)
        sdk.clients.ble.startScan()
    }

    fun stopScanning() {
        view?.showProgress(false)
        sdk.clients.ble.stopScan()
    }
}

interface BleClientListView : BaseView {
    fun showProgress(show: Boolean)
    fun onListUpdated(listOfClients: List<BleClient>)
}

class DeviceComparable : Comparator<BleClient> {
    override fun compare(o1: BleClient?, o2: BleClient?): Int {
        if (o1!!.gbd == null) {
            if (o2!!.gbd == null) {
                return 0
            } else {
                return 1
            }
        } else if (o2!!.gbd == null) {
            return -1
        }


        if (o1!!.gbd!!.isGeenyDevice) {
            if (o2!!.gbd!!.isGeenyDevice) {
                return compareName(o1, o2)
            } else {
                return -1
            }
        } else if (o2!!.gbd!!.isGeenyDevice) {
            return 1
        }

        return compareName(o1, o2)
    }

    private fun compareName(o1: BleClient, o2: BleClient): Int {
        val name1: String? = o1.name()
        val name2: String = o2.name() ?: return -1

        if(name1 == null) {
            return 1
        }

        val res = name1.compareTo(name2)

        if (res == 0) {
            return compareAddress(o1, o2)
        }

        return res
    }

    private fun compareAddress(o1: BleClient, o2: BleClient): Int = o1.gbd!!.address.compareTo(o2.gbd!!.address)
}