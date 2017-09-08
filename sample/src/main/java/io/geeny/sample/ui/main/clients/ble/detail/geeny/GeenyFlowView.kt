package io.geeny.sample.ui.main.clients.ble.detail.geeny

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import io.geeny.sdk.geeny.flow.GeenyFlow
import kotlinx.android.synthetic.main.row_flow.view.*

class GeenyFlowView : LinearLayout {


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
        orientation = LinearLayout.VERTICAL

    }

    fun bind(flow: GeenyFlow) {
        val routes = flow.routes

        var s: String = ""
        for (route in routes) {
            s += route.identifier() + "\n"
        }
        textViewGeenyFlowAll.text = s
    }
}