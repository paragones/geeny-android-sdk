package io.geeny.sample.ui.common.presenter

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter<V : BaseView> : Presenter<V> {
    var view: V? = null
    var disposables: CompositeDisposable? = null

    override fun attach(view: V) {
        this.view = view
        disposables = CompositeDisposable()
    }

    override fun detach() {
        this.view = null
        disposables?.dispose()
        disposables = null
    }

    fun add(disposable: Disposable) {
        disposables?.add(disposable)
    }

    fun showError(throwable: Throwable) {
        view!!.showError()(throwable)
    }

    fun error(): (Throwable) -> Unit = view!!.showError()
}