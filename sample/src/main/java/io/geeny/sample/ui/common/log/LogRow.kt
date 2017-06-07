package io.geeny.sample.ui.common.log

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sdk.clients.ble.GattResult
import kotlinx.android.synthetic.main.row_log.view.*

class LogRow : CardView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {}

    fun bind(result: GattResult) {
        textViewLogMessage.text = result.type.toString() + "\n" + "Source: " + result.property.label
        textViewDate.text = result.formattedDate()
    }

    fun setDate(result: GattResult) {
        textViewDate.text = result.formattedDate()
    }

    fun setLogMessage(msg: String) {
        textViewLogMessage.text = msg
    }
}