package io.geeny.sample.ui.common.presenter

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.geeny.sample.GatewayApp

abstract class BaseFragment : Fragment(), BaseView {
    var dialog: Dialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(layout(), container, false)

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(): (Throwable) -> Unit = {
        dialog = defaultErrorDialog(context, it)
        dialog?.show()
    }

    override fun progress(show: Boolean) {
        progressView()?.visibility = if (show) View.VISIBLE else View.GONE
    }

    open fun progressView(): View? {
        return null
    }

    fun setTitle(title: String) {
        (activity as AppCompatActivity).supportActionBar!!.title = title
    }

    override fun back() {
        activity.onBackPressed()
    }

    fun app(): GatewayApp = GatewayApp.from(context)
    fun component() = app().component
    fun sdk() = component().sdk

    abstract fun layout(): Int
}