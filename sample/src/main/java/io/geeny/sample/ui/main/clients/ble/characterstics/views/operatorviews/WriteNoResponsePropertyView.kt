package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sdk.clients.ble.BleClient

class WriteNoResponsePropertyView : FrameLayout, ConnectionView {

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

    }
}