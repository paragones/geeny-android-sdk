package io.geeny.sample.ui.main.geeny.blethinglist

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import kotlinx.android.synthetic.main.row_ble_thing.view.*

class BleThingRow : CardView {

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

    fun bind(deviceInfo: DeviceInfo) {
        labeledBleName.content = deviceInfo.deviceName
        labeledBleThingType.content = deviceInfo.thingTypeId.toString()
    }
}