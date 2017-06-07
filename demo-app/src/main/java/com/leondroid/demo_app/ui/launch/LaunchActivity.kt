package com.leondroid.demo_app.ui.launch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.leondroid.demo_app.R
import com.leondroid.demo_app.ui.main.MainActivity
import com.leondroid.demo_app.ui.signin.SignInActivity
import io.geeny.sample.ui.common.presenter.BaseActivity

class LaunchActivity : BaseActivity(), LaunchView {
    private lateinit var presenter: LaunchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        presenter = LaunchPresenter(sdk(), app().ioScheduler, app().mainScheduler)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(SignInActivity.TAG, "onNewIntent: " + intent)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun openSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
    }

    override fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
    }

}