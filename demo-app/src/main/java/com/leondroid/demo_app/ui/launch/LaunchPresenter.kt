package com.leondroid.demo_app.ui.launch

import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.reactivex.Scheduler

class LaunchPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<LaunchView>() {

    fun load() {
        sdk.geeny.auth.isSignedIn()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    if (it) {
                        view?.openMain()
                    } else {
                        view?.openSignIn()
                    }
                }
    }
}

interface LaunchView : BaseView {
    fun openSignIn()
    fun openMain()
}