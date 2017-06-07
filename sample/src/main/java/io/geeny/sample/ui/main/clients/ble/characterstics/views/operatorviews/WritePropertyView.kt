package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.view_characteristic_write.view.*

class WritePropertyView : CardView, ConnectionView {

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

    override fun bind(connection: BleClient, characteristic: BluetoothGattCharacteristic) {
        buttonCharWrite.setOnClickListener {
            val t = editCharTextWrite.text.toString()
            if (!t.isEmpty()) {
                val l: Byte = t.toInt().toByte()

                val array = ByteArray(1)
                array[0] = l
                connection.write(characteristic, array)
            }
        }
    }
}