package io.geeny.sample.ui.main.routing.router.list

import android.content.Context
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.usecases.DeleteAllRoutes
import io.geeny.sdk.routing.router.usecases.StartAllRoutes
import io.reactivex.Scheduler

class RouterListPresenter(
        private val router: Router,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler) : BasePresenter<RouterListView>() {
    fun load() {
        add(
                router.list()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showRoutes(it) },
                                error()
                        )

        )
    }

    fun openRoute(route: Route) {
        view?.openRoute(route)
    }

    fun deleteAll() {
        DeleteAllRoutes().delete(router)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    view?.toast("Delete All routes: " + it.size)
                    view?.onDeletedAllRoutes()
                }
    }

    fun startAll(context: Context) {
        StartAllRoutes.startAll(router, context)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    view?.toast("Started All routes: " + it.size)
                }
    }
}

interface RouterListView : BaseView {
    fun showRoutes(routes: List<Route>)
    fun openRoute(route: Route)
    fun onDeletedAllRoutes()
}