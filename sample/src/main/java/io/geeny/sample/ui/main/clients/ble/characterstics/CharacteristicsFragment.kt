package io.geeny.sample.ui.main.clients.ble.characterstics

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.GattResult
import kotlinx.android.synthetic.main.fragment_ble_client_characteristic.*

class CharacteristicsFragment : BaseFragment(), CharacteristicView {

    var presenter: CharacteristicsPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = GatewayApp.from(activity)
        presenter = CharacteristicsPresenter(
                arguments.getString(ARG_ADDRESS),
                arguments.getString(ARG_CHARACTERISTIC_ID),
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
    }

    override fun layout(): Int = R.layout.fragment_ble_client_characteristic

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.offscreenPageLimit = 3
        connectionLayoutChar.connect(arguments.getString(ARG_ADDRESS))
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

    override fun onConnectionLoaded(connection: BleClient) {
        textViewBleAddress.content = connection.address()
        textViewBleCharacteristicUUID.content = arguments.getString(ARG_CHARACTERISTIC_ID)
    }

    override fun onCharacteristicLoaded(characteristic: BluetoothGattCharacteristic, connection: BleClient) {
        viewPager.adapter = Adapter(connection, characteristic)
    }


    override fun onValueChanged(result: GattResult) {
        textViewBleCurrentValue.content = result.formattedValue()
        textViewBleLastDate.content = result.formattedDate()
    }

    companion object {
        val TAG: String = CharacteristicsFragment::class.java.simpleName
        val ARG_ADDRESS: String = "ARG_ADDRESS"
        val ARG_CHARACTERISTIC_ID: String = "ARG_CHARACTERISTIC_ID"

        fun newInstance(address: String, characteristicId: String): CharacteristicsFragment {
            val fragment = CharacteristicsFragment()
            val arguments = Bundle()
            arguments.putString(ARG_ADDRESS, address)
            arguments.putString(ARG_CHARACTERISTIC_ID, characteristicId)

            fragment.arguments = arguments
            return fragment
        }
    }
}

class Adapter(private val connection: BleClient, private val characteristic: BluetoothGattCharacteristic) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup?, position: Int): Any {

        val viewId = when (position) {
            0 -> R.layout.routing_layout
            1 -> R.layout.view_characteristic_control
            2 -> R.layout.view_characteristic_log
            else -> 0
        }

        val view = LayoutInflater.from(container!!.context).inflate(viewId, container, false)
        (view as ConnectionView).bind(connection, characteristic)
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View?, o: Any?): Boolean = o == view

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "Routing"
        1 -> "Controller"
        2 -> "Log"
        else -> "Unknown"
    }


    override fun getCount(): Int = 2

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}