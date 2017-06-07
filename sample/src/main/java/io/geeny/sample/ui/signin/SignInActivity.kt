package io.geeny.sample.ui.signin

import android.content.Intent
import android.os.Bundle
import android.view.View
import io.geeny.sample.ApplicationComponent
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseActivity
import io.geeny.sample.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity(), SignInView {

    var presenter: SignInPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val com: ApplicationComponent = GatewayApp.from(this).component
        presenter = SignInPresenter(com.sdk, com.mainScheduler, com.ioScheduler)

        setContentView(R.layout.activity_sign_in)
    }


    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        buttonSignIn.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                toast("You need to provide email and password")
            } else {
                presenter?.signIn(email, password)
            }
        }
        buttonSkipSignIn.setOnClickListener { presenter?.skip() }
    }

    override fun onStop() {
        presenter?.detach()
        buttonSkipSignIn.setOnClickListener(null)
        buttonSignIn.setOnClickListener(null)
        super.onStop()
    }

    override fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
    }

    override fun progressView(): View? {
        return progressLayout
    }
}
