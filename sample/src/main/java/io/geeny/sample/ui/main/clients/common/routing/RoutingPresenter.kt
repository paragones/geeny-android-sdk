package io.geeny.sample.ui.main.clients.common.routing

import android.content.Context
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.custom.slots.ResourceType
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.EmptyRoute
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteType
import io.reactivex.Scheduler

class RoutingPresenter(
        private val type: RouteType,
        private val clientAddress: String,
        private val resourceId: String,
        private val resourceType: ResourceType,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<RoutingView>() {

    val router = sdk.routing.router

    fun load() {

        when (resourceType) {
            ResourceType.SINK -> {
                view?.hideProducer()
                loadConsumerRoute()
            }
            ResourceType.SOURCE -> {
                view?.hideConsumer()
                loadProducerRoute()
            }
            ResourceType.CHANNEL -> {
                loadConsumerRoute()
                loadProducerRoute()
            }
        }
    }

    private fun loadProducerRoute() {
        add(
                router.get(type, clientAddress, resourceId, Direction.PRODUCER)
                        .defaultIfEmpty(EmptyRoute())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }

    private fun loadConsumerRoute() {
        add(
                router.get(type, clientAddress, resourceId, Direction.CONSUMER)
                        .defaultIfEmpty(EmptyRoute())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }

    fun registerConsumer(topic: String) {
        add(
                router.create(type, Direction.CONSUMER, topic, clientAddress, resourceId)
                        .defaultIfEmpty(EmptyRoute())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }


    fun registerProducer(topicJournalType: TopicJournalType) {
        add(
                router.create(type, Direction.PRODUCER, resourceId, clientAddress, resourceId, topicJournalType)
                        .defaultIfEmpty(EmptyRoute())
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }


    private fun routeLoaded(): (Route) -> Unit = {
        if (it.isEmpty()) {
            view?.noRouteRegisteredYet(it)
        } else {
            view?.onRouteLoaded(it)
        }
    }

    fun delete(direction: Direction) {
        add(
                router.get(type, clientAddress, resourceId, direction)
                        .flatMap {
                            router.remove(it.info())
                        }
                        .map { EmptyRoute(direction = direction) }
                        .defaultIfEmpty(EmptyRoute(direction = direction))
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }

    fun start(direction: Direction, context: Context) {
        add(
                router.get(type, clientAddress, resourceId, direction)
                        .map {
                            it.start(context)
                            it
                        }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }

    fun stop(direction: Direction) {
        add(
                router.get(type, clientAddress, resourceId, direction)
                        .map {
                            it.stop()
                            it
                        }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(routeLoaded())
        )
    }

}

interface RoutingView : BaseView {
    fun noRouteRegisteredYet(emptyRoute: Route)
    fun onRouteLoaded(route: Route)
    fun hideProducer()
    fun hideConsumer()
}