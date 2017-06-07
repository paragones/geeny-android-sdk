package io.geeny.sample.ui.main.routing.router.detail

import android.content.Context
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.Router
import io.reactivex.Scheduler

class RouterDetailPresenter(
        private val id: String,
        private val router: Router,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler) : BasePresenter<RouterDetailView>() {


    fun load() {
        add(
                router.get(id)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { onRouteLoaded(it) },
                                view?.showError()
                        ))
    }

    private fun onRouteLoaded(route: Route) {
        view?.routeLoaded(route)

        add(
                route.running()
                        .observeOn(mainScheduler)
                        .subscribe {
                            view?.onRunningStateHasChanged(it)
                        }
        )
    }

    fun delete(route: Route) {
        add(
                router.remove(route.info())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                {
                                    view?.toast("Route ${route.info().identifier()} deleted")
                                    view?.back()
                                },
                                view?.showError()
                        )
        )
    }

    fun stop() {

        add(
                router.get(id)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { it.stop() },
                                view?.showError()
                        )
        )
    }

    fun start(context: Context) {

        add(
                router.get(id)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { it.start(context) },
                                view?.showError()
                        )
        )
    }
}

interface RouterDetailView : BaseView {
    fun routeLoaded(route: Route)
    fun onRunningStateHasChanged(state: ConnectionState)
}