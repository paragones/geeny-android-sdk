package io.geeny.sample.ui.main.clients.common.routing

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.geeny.sample.R
import kotlinx.android.synthetic.main.layout_routing_control.view.*

class RoutingControlLayout : LinearLayout {


    var callback: Callback? = null

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
        orientation = LinearLayout.HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.layout_routing_control, this, true)
        buttonDeleteRoute.setOnClickListener {
            callback?.onDeleteRoute()
        }
    }

    fun setIsStarted(isStarted: Boolean) {
        if (isStarted) {
            buttonStartOrStopRoute.text = "Stop"
            buttonStartOrStopRoute.setOnClickListener {
                callback?.onStopRoute()
            }
        } else {
            buttonStartOrStopRoute.text = "Start"
            buttonStartOrStopRoute.setOnClickListener {
                callback?.onStartRoute()
            }
        }
    }

    interface Callback {
        fun onStartRoute()
        fun onStopRoute()
        fun onDeleteRoute()
    }
}