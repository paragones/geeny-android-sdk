package com.leondroid.demo_app.ui.signin

import android.content.Context
import android.content.Intent
import android.util.Log
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.geeny.auth.AuthenticationComponent
import io.geeny.sdk.geeny.auth.Credentials
import io.geeny.sdk.geeny.auth.isValid
import io.reactivex.Observable
import io.reactivex.Scheduler

class SignInPresenter(
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<SignInPresenterView>() {


    override fun attach(view: SignInPresenterView) {
        super.attach(view)
        add(
                sdk.geeny.auth.state.connect().subscribe {
                    Log.d(TAG, "Current state is: " + it)
                    if(it == AuthenticationComponent.State.SIGNED_IN) {
                        view.openMain()
                    }
                }
        )
    }

    fun onSignInClicked(context: Context) {
        add(
                sdk.geeny.auth.signIn(context)
                        .subscribe(
                                {
                                    val msg = if (it) "was successful" else "failed"
                                    Log.d(TAG, "SignIn request $msg")
                                },
                                view?.showError()
                        )
        )

    }

    fun onNewIntent(intent: Intent) {
        add(
                sdk.geeny.auth.onNewIntent(intent)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { Log.d(TAG, "onNewIntent return is: " + it) },
                                view?.showError()
                        )
        )
    }

    fun onSignIn(email: String, password: String) {
//        val c = Credentials(email, password)
//        if(c.isValid()) {
//
//        } else{
//            view?.toast("Credentials not complete!")
//        }
        val c = Credentials("lenny@geeny.io", "qqqqqqqq")

        add(
                sdk.geeny.auth.signInWithCredentials(c)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(
                                { Log.d(TAG, "SignedIn: " + it) },
                                view?.showError()
                        )
        )

    }


    companion object {
        val TAG = SignInPresenter::class.java.simpleName
    }

}

interface SignInPresenterView : BaseView {
    fun openMain()
}