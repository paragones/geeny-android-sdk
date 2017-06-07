package io.geeny.sample.ui.main.clients.ble.detail.general.view

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.geeny.sdk.clients.ble.CharacteristicProperty
import kotlinx.android.synthetic.main.view_gatt_characterstics.view.*

class CharacteristicsServiceView : FrameLayout {

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

    fun setCharacteristic(characteristic: BluetoothGattCharacteristic) {
        with(this) {

            textViewGattCharacteristicsUUID.text = "${characteristic.uuid}"

            val properties = characteristic.properties

            val list = CharacteristicProperty.list(properties)

            var count = list.size
            var s = "("
            for (characterisiticProperty in list) {
                s += characterisiticProperty.label
                if (count-- > 1) {
                    s += ", "
                }
            }
            s += ")"

            textViewGattCharacteristicsProperties.text = s
        }
    }
}