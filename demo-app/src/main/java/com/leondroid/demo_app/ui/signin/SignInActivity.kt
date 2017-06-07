package com.leondroid.demo_app.ui.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.leondroid.demo_app.R
import com.leondroid.demo_app.ui.main.MainActivity
import io.geeny.sample.ui.common.presenter.BaseActivity
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : BaseActivity(), SignInPresenterView {
    private lateinit var presenter: SignInPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        presenter = SignInPresenter(sdk(), app().ioScheduler, app().mainScheduler)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: " + intent)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)



        buttonSignIn.setOnClickListener {
            onSignInClicked()
        }


        if (intent != null) {
            presenter.onNewIntent(intent)
        }

    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    fun onSignInClicked() {
        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()
        presenter.onSignIn(email, password)
    }

    override fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
    }

    companion object {
        val TAG = SignInActivity::class.java.simpleName
    }

    fun isOAuthIntent(intent: Intent): Boolean {
        val data = intent.data
        if (data != null) {
            data.host
            Log.d(TAG, "Data: " + data)
        }

        return false
    }
}