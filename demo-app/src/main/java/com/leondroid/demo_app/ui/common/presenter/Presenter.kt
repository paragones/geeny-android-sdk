package io.geeny.sample.ui.common.presenter

interface Presenter<V: PresenterView> {
    fun attach(view: V)
    fun detach()
}