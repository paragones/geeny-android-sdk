package io.geeny.sample.ui.common.log

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import io.geeny.sample.GatewayApp
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.disposables.Disposable

class LogView : RecyclerView, ConnectionView {

    private var disposable: Disposable? = null
    private val logAdapter: LogAdapter = LogAdapter()

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
        hasFixedSize()
        layoutManager = LinearLayoutManager(context)
        adapter = logAdapter
    }

    override fun bind(connection: BleClient, characteristic: BluetoothGattCharacteristic) {
        val ms = GatewayApp.from(context).component.mainScheduler
        disposable = connection.callback(characteristic)
                .observeOn(ms)
                .subscribe { logAdapter.add(it) }
    }

    override fun onDetachedFromWindow() {
        disposable?.dispose()
        super.onDetachedFromWindow()
    }
}
