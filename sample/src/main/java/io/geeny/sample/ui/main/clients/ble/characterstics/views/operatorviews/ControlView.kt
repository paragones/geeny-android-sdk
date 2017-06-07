package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.CharacteristicProperty

class ControlView : ScrollView, ConnectionView {

    private var container: LinearLayout? = null

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
        container = LinearLayout(context)
        container?.orientation = LinearLayout.VERTICAL
        addView(container)
    }

    override fun bind(connection: BleClient, characteristic: BluetoothGattCharacteristic) {
        CharacteristicProperty.list(characteristic.properties).map { instantiateItem(it, connection, characteristic) }.forEach {
            container?.addView(it)
        }
    }

    fun instantiateItem(property: CharacteristicProperty, connection: BleClient, characteristic: BluetoothGattCharacteristic): android.view.View {
        val viewId: Int = when (property) {
            CharacteristicProperty.PROPERTY_EXTENDED_PROPS -> R.layout.view_characteristic_write_extended_props
            CharacteristicProperty.PROPERTY_INDICATE -> R.layout.view_characteristic_indicate
            CharacteristicProperty.PROPERTY_NOTIFY -> R.layout.view_characteristic_notify
            CharacteristicProperty.PROPERTY_READ -> R.layout.view_characteristic_read
            CharacteristicProperty.PROPERTY_SIGNED_WRITE -> R.layout.view_characteristic_signed_write
            CharacteristicProperty.PROPERTY_WRITE -> R.layout.view_characteristic_write
            CharacteristicProperty.PROPERTY_WRITE_NO_RESPONSE -> R.layout.view_characteristic_write_no_response
        }

        val view = LayoutInflater.from(container!!.context).inflate(viewId, container, false)
        (view as ConnectionView).bind(connection, characteristic)

        return view
    }
}