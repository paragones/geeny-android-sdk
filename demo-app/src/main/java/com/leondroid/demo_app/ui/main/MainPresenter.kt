package com.leondroid.demo_app.ui.main

import android.util.Log
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.geeny.auth.AuthenticationComponent
import io.reactivex.Scheduler

class MainPresenter(val sdk: GeenySdk, val ioScheduler: Scheduler, val mainScheduler: Scheduler) : BasePresenter<MainView>() {

    override fun attach(view: MainView) {
        super.attach(view)
        add(
                sdk.geeny.auth.state.connect()
                        .subscribe {
                            if (it == AuthenticationComponent.State.SIGNED_OUT) {
                                view.restartApp()
                            }
                        }
        )
    }

    fun signout() {
        add(
                sdk.geeny.auth.signOut()
                        .subscribe {
                            Log.d("MainPresenter", it.toString())
                        }
        )
    }

    fun startScan() {
        sdk.clients.ble.startScan()
    }


    fun stopScan() {
        sdk.clients.ble.stopScan()
    }
}

interface MainView : BaseView {
    fun restartApp()
}