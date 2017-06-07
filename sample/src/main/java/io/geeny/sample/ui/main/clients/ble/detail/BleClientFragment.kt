package io.geeny.sample.ui.main.clients.ble.detail

import android.content.Context
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sample.ui.main.clients.ble.detail.geeny.DetailGeenyLayout
import io.geeny.sample.ui.main.clients.ble.detail.general.DetailGeneralFragment
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.fragment_ble_client_detail.*


class BleClientFragment :
        BaseFragment(),
        BleConnectionView,
        BleClientContainer {
    var presenter: BleClientPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = GatewayApp.from(activity)

        presenter = BleClientPresenter(
                arguments.getString(ARG_ADDRESS),
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler
        )
    }

    override fun layout(): Int = R.layout.fragment_ble_client_detail

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerGatt.offscreenPageLimit = 4
        val address = arguments.getString(ARG_ADDRESS)
        viewPagerGatt.adapter = DetailPagerAdapter(address, context, this)
        connectionLayout.connect(address)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        presenter?.load()
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    override fun onConnectionLoaded(client: BleClient) {
        setTitle(client.gbd?.name ?: "device not available yet")
    }

    override fun onCharacteristicClicked(address: String, characteristicUUID: String) {
        (activity as Container).onCharacteristicClicked(address, characteristicUUID)
    }

    override fun showError(error: Throwable) {
        showError()(error)
    }

    interface Container {
        fun onCharacteristicClicked(address: String, characteristicUUID: String)
    }

    companion object {
        val TAG = BleClientFragment::class.java.simpleName
        val ARG_ADDRESS = "ARG_ADDRESS"

        fun newInstance(address: String): BleClientFragment {
            val fragment = BleClientFragment()

            val arguments = Bundle()
            arguments.putString(ARG_ADDRESS, address)

            fragment.arguments = arguments

            return fragment
        }
    }
}

interface BleClientContainer {
    fun onCharacteristicClicked(address: String, characteristicUUID: String)
    fun toast(message: String)
    fun showError(error: Throwable)
}

class DetailPagerAdapter(val address: String, val context: Context, val container: BleClientContainer) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val view = view(position)
        container!!.addView(view)
        return view
    }

    private fun view(position: Int): View = when (position) {
        0 -> DetailGeneralFragment(context).apply { connect(address, container) }
        1 -> DetailGeenyLayout(context).apply { connect(address, container) }
        else -> View(context)
    }

    override fun isViewFromObject(view: View?, o: Any?): Boolean = o == view

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "General"
        1 -> "Geeny"
        else -> "Not used"
    }

    override fun getCount(): Int = 2

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}