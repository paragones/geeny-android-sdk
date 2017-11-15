package io.geeny.sample.ui.main.clients.custom.slot

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction

class CustomResourcePresenter(
        val clientId: String,
        val resourceId: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<CustomResourceView>() {
    fun load() {
        add(
                sdk.clients.custom.getClient(clientId)
                        .flatMap {
                            Observable.zip<Client, Slot, Pair<Client, Slot>>(
                                    Observable.just(it),
                                    it.getSlot(resourceId),
                                    BiFunction { client, resource -> Pair(client, resource) }
                            )
                        }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.onClientAndResourceLoaded(it.first, it.second) },
                                view?.showError()
                        )
        )
    }


}

interface CustomResourceView : BaseView {
    fun onClientAndResourceLoaded(client: Client, slot: Slot)
}