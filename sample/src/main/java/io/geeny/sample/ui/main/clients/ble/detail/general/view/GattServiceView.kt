package io.geeny.sample.ui.main.clients.ble.detail.general.view

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.detail.general.ServiceAdapter
import kotlinx.android.synthetic.main.view_gatt_characterstics.view.*
import kotlinx.android.synthetic.main.view_gatt_service.view.*

class GattServiceView : CardView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {

    }

    fun bindService(service: BluetoothGattService, callback: ServiceAdapter.Callback?) {
        with(this) {
            textViewServiceId.text = "Service " + service.uuid.toString()
            var first = true
            var count = service.characteristics.size
            service.characteristics.forEach { characteristic ->
                val charView = createCharView(characteristic, containerGattServiceChar, first, count == 1)
                first = false
                count--

                charView.setOnClickListener {
                    callback?.onCharacteristicClicked(characteristic)
                }

                containerGattServiceChar.addView(charView)
            }
        }
    }

    private fun createCharView(it: BluetoothGattCharacteristic, container: ViewGroup, first: Boolean, last: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.view_gatt_characterstics, container, false) as CharacteristicsServiceView

        if (first) {
            view.dividerCharacteristicTop.visibility = View.VISIBLE
        }

        if (last) {
            view.dividerCharacteristicLast.visibility = View.GONE
        }

        view.setCharacteristic(it)
        return view
    }
}