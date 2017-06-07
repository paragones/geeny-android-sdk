package io.geeny.sample.ui.main.clients.ble.common.connection

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sdk.common.ConnectionState
import kotlinx.android.synthetic.main.fragment_detail_connection.view.*

class MqttConnectionLayout : LinearLayout, MqttConnectionView {
    override fun onConnectionLoaded(connection: io.geeny.sdk.clients.mqtt.GeenyMqttClient) {

    }

    override fun back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var presenter: MqttConnectionPresenter? = null
    private var address: String? = null

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
        LayoutInflater.from(context).inflate(R.layout.fragment_detail_connection, this, true)
    }


    fun connect(address: String) {
        this.address = address
        val app = GatewayApp.from(context)
        presenter = MqttConnectionPresenter(address,
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
        loadData()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadData()
    }

    private fun loadData() {
        presenter?.attach(this)
        presenter?.load()

        buttonContainerConnect.setOnClickListener {
            presenter?.connectOrDisconnect(context)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.detach()
    }

    override fun onConnectionStatusHasChanged(connectionState: ConnectionState) {

        val showProgress = when (connectionState) {
            ConnectionState.CONNECTED -> false
            ConnectionState.DISCONNECTED -> false
            ConnectionState.CONNECTING -> true
        }

        progress(showProgress)

        val textColor: Int = when (connectionState) {
            ConnectionState.CONNECTED -> Color.rgb(0, 180, 90)
            ConnectionState.DISCONNECTED -> Color.RED
            ConnectionState.CONNECTING -> Color.DKGRAY
        }

        textViewConnectionStatus.setTextColor(textColor)
        textViewConnectionStatus.text = when (connectionState) {
            ConnectionState.CONNECTING -> "..."
            ConnectionState.DISCONNECTED -> "Disconnected"
            else -> "Connected"
        }

        buttonConnect.text = when (connectionState) {
            ConnectionState.CONNECTING -> "Connecting..."
            ConnectionState.DISCONNECTED -> "Connect"
            else -> "Disconnect"
        }
    }


    override fun showError(): (Throwable) -> Unit = {
    }

    override fun toast(message: String) {
    }

    override fun progress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}