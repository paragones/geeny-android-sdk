package io.geeny.sdk.geeny.flow

import io.geeny.sdk.routing.router.types.Route

data class GeenyFlow(val routes: List<Route>) {
    fun startingResourceId(): String? =
            if (routes.isNotEmpty()) {
                routes[0].info().clientResourceId
            } else {
                null
            }

}
