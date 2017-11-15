package io.geeny.sample.ui.main.clients.custom.list

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.AppClient
import io.reactivex.Scheduler

class CustomClientListPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<CustomClientListView>() {

    fun load() {
        add(
                sdk.clients.custom.availableDevices()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showClients(it) },
                                view?.showError()
                        )
        )
    }
}

interface CustomClientListView : BaseView {
    fun showClients(clients: List<Client>)

}