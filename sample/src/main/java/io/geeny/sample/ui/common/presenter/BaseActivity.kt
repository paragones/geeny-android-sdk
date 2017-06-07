package io.geeny.sample.ui.common.presenter

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import io.geeny.sample.GatewayApp


open class BaseActivity : AppCompatActivity(), BaseView {
    override fun showError(): (Throwable) -> Unit = {
        dialog = defaultErrorDialog(this, it)
        dialog?.show()
    }

    var dialog: Dialog? = null

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun progress(show: Boolean) {
        progressView()?.visibility = if (show) View.VISIBLE else View.GONE
    }

    open fun progressView(): View? = null

    override fun back() {
        onBackPressed()
    }


    fun app(): GatewayApp = GatewayApp.from(this)
    fun component() = app().component
    fun sdk() = component().sdk
}