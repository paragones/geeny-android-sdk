package com.leondroid.demo_app.ui.main.blething

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.common.toHex
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteType
import kotlinx.android.synthetic.main.row_geeny_flow.view.*

class GeenyFlowView : LinearLayout {
    var callback: Callback? = null

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

    fun bind(flow: GeenyFlow, callback: Callback) {
        this.callback = callback
        // put infos
        textViewResourceId.text = flow.startingResourceId()
        flow.routes.filter {it.info().type == RouteType.BLE || it.info().type == RouteType.CUSTOM}.forEach { textViewFlowTitle.text = "Resource (${it.info().direction})" }
        flow.routes.filter {it.info().type == RouteType.BLE}.forEach { labelSourceType.text = "Ble Status" }
        flow.routes.filter {it.info().type == RouteType.CUSTOM}.forEach { labelSourceType.text = "Virtual Status" }

        buttonTriggerConnect.setOnClickListener {
            this.callback?.start(flow)
        }

        buttonTriggerRead.setOnClickListener {
            if (flow.startingResourceId() != null) {
                this.callback?.read(flow.startingResourceId()!!)
            }
        }
    }

    fun setCurrentValue(bytes: ByteArray) {
        textViewCurrentValue.isSelected = true
        textViewCurrentValue.text = bytes.toHex().trim()
    }

    fun onConnectionStatusHasChanged(route: Route, status: ConnectionState) {
        val statusView = when (route.info().type) {
            RouteType.MQTT -> textViewConnectionStatusMqtt
            RouteType.BLE -> textViewConnectionStatusBle
            RouteType.CUSTOM -> textViewConnectionStatusBle
            else -> null
        }

        statusView?.text = when (status) {
            ConnectionState.CONNECTED -> "connected"
            ConnectionState.DISCONNECTED -> "disconnected"
            ConnectionState.CONNECTING -> "..."
        }

        statusView?.setTextColor(when (status) {
            ConnectionState.CONNECTED -> Color.GREEN
            ConnectionState.DISCONNECTED -> Color.RED
            ConnectionState.CONNECTING -> Color.GRAY
        })
    }

    interface Callback {
        fun start(flow: GeenyFlow)
        fun read(resourceId: String)
    }
}