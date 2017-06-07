package io.geeny.sample.ui.main.geeny.blethinglist

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.reactivex.Scheduler

class BleThingListPresenter(
        private val sdk: GeenySdk,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler
) : BasePresenter<BleThingListView>(){

    fun load() {
        add(
                sdk.geeny.deviceInfoRespository.list()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {view?.showList(it)},
                                view?.showError()
                        )
        )
    }
}

interface BleThingListView: BaseView {
    fun showList(list: List<DeviceInfo>)
}