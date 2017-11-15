package com.leondroid.demo_app.ui.main.blethings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.row_things.view.*

class ThingsRow : FrameLayout {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

    }

    fun bind(client: BleClient) {
        imageViewGeenyLogo.visibility = if (client.isGeenyDevice()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        textViewThingsRowTitle.text = client.name()
        textViewThingsRowAddress.text = client.address()
    }
}