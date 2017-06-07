package io.geeny.sample.ui.main.routing.broker

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo
import io.geeny.sdk.routing.router.types.RouteInfo
import io.geeny.sdk.routing.router.Router
import io.reactivex.Scheduler

class BrokerListPresenter(
        private val router: Router,
        private val broker: BoteBroker,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler) : BasePresenter<BrokerView>() {
    fun load() {
        add(
                broker.list()
                        .flatMapIterable { it }
                        .flatMap {
                            val info = it
                            router.loadRoutesWithTopic(info.id).map { Pair(info, it) }
                        }
                        .toList()
                        .toObservable()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.onTopicsLoaded(it) },
                                error()
                        )
        )
    }
}

interface BrokerView : BaseView {
    fun onTopicsLoaded(infos: List<Pair<TopicInfo, List<RouteInfo>>>)
}