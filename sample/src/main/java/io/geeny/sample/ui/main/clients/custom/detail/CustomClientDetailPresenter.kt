package io.geeny.sample.ui.main.clients.custom.detail

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction

class CustomClientDetailPresenter(
        val address: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<CustomClientDetailView>() {

    fun load() {
        add(
                sdk.clients.custom.getClient(address)
                        .flatMap {
                            Observable.zip<Client, List<Slot>, Pair<Client, List<Slot>>>(
                                    Observable.just(it),
                                    it.slots(),
                                    BiFunction { client, resourceList ->
                                        Pair(client, resourceList)
                                    })
                        }
                        .subscribe(
                                { view?.showClient(it.first, it.second) },
                                view?.showError()
                        )
        )
    }
}

interface CustomClientDetailView : BaseView {
    fun showClient(customClient: Client, slotList: List<Slot>)
}