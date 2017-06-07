package io.geeny.sample.ui.signin

import android.text.Editable
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.geeny.auth.Credentials

class SignInPresenter(val sdk: GeenySdk,
                      val mainScheduler: io.reactivex.Scheduler,
                      val ioScheduler: io.reactivex.Scheduler) : io.geeny.sample.ui.common.presenter.BasePresenter<SignInView>() {

    fun signIn(email: String, password: String) {
        add(
                sdk.geeny.auth.signInWithCredentials(Credentials(email, password))
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .doOnSubscribe { view?.progress(true) }
                        .subscribe(
                                { view?.openMain() },
                                view?.showError(),
                                { view?.progress(false) }
                        )
        )
    }

    fun skip() {
        view?.openMain()
    }

}


interface SignInView : io.geeny.sample.ui.common.presenter.BaseView {
    fun openMain()
}