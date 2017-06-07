package io.geeny.sample.ui.main.clients.common.routing.consume

import io.geeny.sample.ui.common.presenter.Presenter
import io.geeny.sample.ui.common.presenter.PresenterView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class TopicPresenter(val sdk: GeenySdk, val ioScheduler: Scheduler, val mainScheduler: Scheduler): Presenter<TopicView> {


    private var compositeDisposable: CompositeDisposable? = null
    private var view: TopicView? = null

    override fun attach(view: TopicView) {
        this.view = view
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
    }

    override fun detach() {
        compositeDisposable?.dispose()
    }

    fun load() {
        sdk.routing.broker.list()
                .defaultIfEmpty(ArrayList())
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(
                    {view?.showTopics(it)}
                )
    }

}

interface TopicView: PresenterView {
    fun showTopics(topics: List<TopicInfo>)
}