package io.geeny.sdk.routing.router.usecases

import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.RouteInfo
import io.reactivex.Observable

class DeleteAllRoutes() {
    fun delete(router: Router): Observable<List<RouteInfo>> =
            router.list()
                    .flatMapIterable { it }
                    .flatMap {
                        router.remove(it.info())
                    }
                    .toList().toObservable()

}