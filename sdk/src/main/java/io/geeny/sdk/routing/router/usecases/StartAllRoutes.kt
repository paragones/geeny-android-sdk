package io.geeny.sdk.routing.router.usecases

import android.content.Context
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.Route
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class StartAllRoutes {

    val map: MutableMap<String, Disposable> = HashMap()

    fun startAll(router: Router, context: Context): Observable<List<Route>> =
            router.list()
                    .flatMapIterable { it }
                    .flatMap {
                        start(it, context)
                    }
                    .toList().toObservable()


    private fun start(route: Route, context: Context): Observable<Route> = Observable.create { subscriber ->
        map.put(route.identifier(), route.running()
                .subscribe {
                    when (it) {
                        ConnectionState.CONNECTED -> {
                            map[route.identifier()]?.dispose()
                            subscriber.onNext(route)
                            subscriber.onComplete()
                        }
                        ConnectionState.DISCONNECTED -> {
                            route.start(context)
                        }
                        ConnectionState.CONNECTING -> {
                        }
                    }
                }
        )
    }
}