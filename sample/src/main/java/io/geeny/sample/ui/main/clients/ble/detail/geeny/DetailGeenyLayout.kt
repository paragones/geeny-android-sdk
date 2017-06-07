package io.geeny.sample.ui.main.clients.ble.detail.geeny

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.fragment_detail_geeny.view.*

import io.geeny.sample.ui.main.clients.ble.detail.BleClientContainer
import io.geeny.sdk.common.GLog
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.geeny.sdk.geeny.flow.GeenyFlow

class DetailGeenyLayout : FrameLayout, DetailGeenyView {
    override fun onFlowLoaded(flows: List<GeenyFlow>) {
        for (flow in flows) {
            val view = LayoutInflater.from(context).inflate(R.layout.row_flow, containerGeenyFlow, false) as GeenyFlowView
            view.bind(flow)
            containerGeenyFlow.addView(view)
        }
    }


    override fun onDeviceIsUnregistered() {
        containerRegistration.visibility = View.VISIBLE
        containerRegistration.setOnClickListener{
            presenter?.register()
        }
    }

    override fun publishGeenyInformation(deviceInfo: DeviceInfo) {

        labeledGeenyBleName.content = deviceInfo.deviceName
        labeledGeenyBleProtocol.content = deviceInfo.protocolVersion.toString()
        labeledGeenyBleSerialNumber.content = deviceInfo.serialNumber.toString()
        labeledGeenyBleThingType.content = deviceInfo.thingTypeId.toString()
    }

    override fun back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var presenter: DetailGeenyPresenter? = null
    private var container: BleClientContainer? = null
    private var address: String? = null
    private var innerView: View? = null


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
        innerView = LayoutInflater.from(context).inflate(R.layout.fragment_detail_geeny, this, false)
        addView(innerView)
    }

    fun connect(address: String, container: BleClientContainer) {
        this.address = address
        this.container = container
        val app = GatewayApp.from(context)

        presenter = DetailGeenyPresenter(address,
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter?.attach(this)
        presenter?.load()

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.detach()
    }

    override fun onConnectionLoaded(connection: BleClient) {
    }

    override fun showError(): (Throwable) -> Unit = {
        GLog.e("FUCK", it.message, it)
        container?.showError(it)
    }

    override fun toast(message: String) {
        container?.toast(message)
    }

    override fun progress(show: Boolean) {
        GLog.d("FUCK", "progress")
        progressBarRegister.visibility = if(show) View.VISIBLE else View.GONE
    }
}