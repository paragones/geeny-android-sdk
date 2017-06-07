package io.geeny.sample.ui.main.host.chart

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.custom.CustomClientPool
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.TypeConverters
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

class ChartPresenter(val sdk: GeenySdk, val ioScheduler: Scheduler, val mainScheduler: Scheduler) : BasePresenter<ChartView>() {

    private var valueDisposable: Disposable? = null

    override fun detach() {
        super.detach()
        valueDisposable?.dispose()
    }

    fun loadResources() {
        add(
                sdk.clients.custom.getClient(CustomClientPool.APP_CLIENT_ADDRESS)
                        .flatMap { it.resources() }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showResources(it) },
                                view?.showError()
                        )
        )
    }


    fun pickResource(resourceId: String) {
        valueDisposable?.dispose()
        chartData = ChartData(ArrayList(), 0, 0, 0, 0)
        valueDisposable = sdk.clients.custom.getClient(CustomClientPool.APP_CLIENT_ADDRESS)
                        .flatMap {
                            it.notify(resourceId, true)
                            it.value(resourceId)
                        }
                        .flatMap { process(it) }
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { view?.showData(it) },
                                view?.showError()
                        )

    }

    var chartData: ChartData<Int> = ChartData(ArrayList(), 0, 0, 0, 0)

    private fun process(array: ByteArray): Observable<ChartData<Int>> = Observable.create {

        GLog.d("ChartPresenter", "process")

        val value = TypeConverters.bytesToInt(array)
        chartData.data.add(value)

        val newData = chartData.copy(
                minY = if (chartData.minY < value) chartData.minY else value,
                maxY = if (chartData.maxY > value) chartData.maxY else value,
                maxX = chartData.maxX + 1
        )

        chartData = newData
        it.onNext(newData.copy())
        it.onComplete()
    }
}

interface ChartView : BaseView {
    fun showResources(slots: List<Slot>)
    fun showData(chartData: ChartData<Int>)
}