package io.geeny.sample.ui.main.clients.ble.list.view

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import io.geeny.sample.GatewayApp
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.GeenyBleDevice
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.row_ble.view.*

class BleClientListRow : CardView {

    private var subscription: Disposable? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

    }

    fun bindConnection(client: BleClient) {

        textViewName.text = client.name()
        textViewAddress.text = client.address()

        if (client.gbd != null) {
            val device: GeenyBleDevice = client.gbd!!
            subscription = client.connection()
                    .observeOn(GatewayApp.from(context).component.mainScheduler)
                    .subscribe {
                        textViewConnectionStatus.text = it.toString()
                        val textColor = when (it) {
                            ConnectionState.CONNECTED -> Color.GREEN
                            ConnectionState.DISCONNECTED -> Color.RED
                            ConnectionState.CONNECTING -> Color.RED
                        }
                        textViewConnectionStatus.setTextColor(textColor)
                    }
            textViewType.text = device.serviceId
            imageViewLogo.visibility = if (device.isGeenyDevice) View.VISIBLE else View.GONE
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscription?.dispose()
        subscription = null
    }
}