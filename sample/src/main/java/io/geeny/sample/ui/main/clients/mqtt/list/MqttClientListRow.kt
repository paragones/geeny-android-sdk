package io.geeny.sample.ui.main.clients.mqtt.list

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.common.ConnectionState
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_mqtt_detail.*
import kotlinx.android.synthetic.main.row_mqtt.view.*

class MqttClientListRow : CardView {

    private var subscription: Disposable? = null

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

    fun bindConnection(client: GeenyMqttClient) {

        textViewMqttClientId.text = client.mqttConfig.clientId
        textViewMqttServerAddress.text = client.mqttConfig.serverUri
        if(client.certificateInfo != null) {
            textViewHasCertificateLoadedRow.text = "Certificate is loaded"
        }

        subscription = client.connection().subscribe {
            textViewConnectionStatusMqtt.text = it.toString()
            val textColor = when (it) {
                ConnectionState.CONNECTED -> Color.GREEN
                ConnectionState.DISCONNECTED -> Color.RED
                ConnectionState.CONNECTING -> Color.RED
            }
            textViewConnectionStatusMqtt.setTextColor(textColor)
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscription?.dispose()
        subscription = null
    }
}