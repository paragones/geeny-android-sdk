package io.geeny.sample.ui.main.clients.ble.detail.general

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.detail.general.view.GattServiceView
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.fragment_detail_general.view.*

import io.geeny.sample.ui.main.clients.ble.detail.BleClientContainer
class DetailGeneralFragment : FrameLayout, DetailGeneralView, ServiceAdapter.Callback {
    override fun back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var connectionPresenter: DetailGeneralPresenter? = null
    private lateinit var innerView: View
    private var container: BleClientContainer? = null
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
        innerView = LayoutInflater.from(context).inflate(R.layout.fragment_detail_general, this, false)
        addView(innerView)
        recyclerViewGeneral.hasFixedSize()
        recyclerViewGeneral.layoutManager = LinearLayoutManager(context)
        recyclerViewGeneral.adapter = ServiceAdapter()
    }


    fun connect(address: String, container: BleClientContainer) {
        this.address = address
        this.container = container
        val app = GatewayApp.from(context)

        connectionPresenter = DetailGeneralPresenter(address,
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        connectionPresenter?.attach(this)
        connectionPresenter?.load()
        (recyclerViewGeneral.adapter as ServiceAdapter).callback = this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (recyclerViewGeneral.adapter as ServiceAdapter).callback = null
        connectionPresenter?.detach()
    }

    override fun onConnectionLoaded(connection: BleClient) {
        labeledName.content =connection.name()
        labeledAddress.content = connection.address()
    }

    override fun onServiceLoaded(services: List<BluetoothGattService>) {
        recyclerViewGeneral.visibility = View.VISIBLE
        (recyclerViewGeneral.adapter as ServiceAdapter).list = services
    }


    override fun onCharacteristicClicked(characteristic: BluetoothGattCharacteristic) {
        container?.onCharacteristicClicked(address!!, characteristic.uuid.toString())
    }

    override fun showError(): (Throwable) -> Unit = {
        container?.showError(it)
    }

    override fun toast(message: String) {
        container?.toast(message)
    }

    override fun progress(show: Boolean) {

    }
}

class ServiceAdapter : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    var callback: Callback? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var list: List<BluetoothGattService>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(list!![position], callback)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent?.context)?.inflate(R.layout.view_gatt_service, parent, false) as GattServiceView)

    override fun getItemCount(): Int = list?.size ?: 0

    class ViewHolder(val serviceView: GattServiceView) : RecyclerView.ViewHolder(serviceView) {
        fun bind(service: BluetoothGattService, callback: Callback?) {
            serviceView.bindService(service, callback)
        }
    }

    interface Callback {
        fun onCharacteristicClicked(characteristic: BluetoothGattCharacteristic)
    }
}