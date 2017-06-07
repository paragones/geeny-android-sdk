package io.geeny.sample.ui.common.presenter

interface BaseView : PresenterView {
    fun showError(): (Throwable) -> Unit

    fun toast(message: String)

    fun progress(show: Boolean)

    fun back()
}