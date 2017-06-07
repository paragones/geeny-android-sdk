package com.leondroid.demo_app.ui.main.things

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.Scheduler
import java.util.*

class ThingsPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<ThingsView>() {

    fun load() {
        sdk.clients.ble.availableDevices()
                .map {
                    Collections.sort(it, DeviceComparable())
                    it
                }
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    view?.onThingsLoaded(it)
                }

    }
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

    fun compareName(o1: BleClient, o2: BleClient): Int {
        val res = o1.name()!!.compareTo(o2.name()!!)
        if (res == 0) {
            return compareAddress(o1, o2)
        }

        return res
    }

    fun compareAddress(o1: BleClient, o2: BleClient): Int = o1.gbd!!.address.compareTo(o2.gbd!!.address)
}

interface ThingsView : BaseView {
    fun onThingsLoaded(it: List<BleClient>)
}