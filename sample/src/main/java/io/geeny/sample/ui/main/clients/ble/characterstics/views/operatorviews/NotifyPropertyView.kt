package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sample.GatewayApp
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_characteristic_notify.view.*

class NotifyPropertyView : CardView, ConnectionView {

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

    private var disposable: Disposable? = null

    override fun bind(connection: BleClient, characteristic: BluetoothGattCharacteristic) {
        val app = GatewayApp.from(context)
        disposable = connection.characteristicState(characteristic)
                .observeOn(app.component.mainScheduler)
                .subscribe { state ->
                    when (state) {
                        ConnectionState.CONNECTED -> {
                            textViewCharConnectionsStatus.text = "Connected"
                            buttonCharNotify.text = "Turn Off"

                            buttonCharNotify.setOnClickListener {
                                connection.notify(characteristic, false)
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            textViewCharConnectionsStatus.text = "Not Connected"
                            buttonCharNotify.text = "Turn On"
                            buttonCharNotify.setOnClickListener {
                                connection.notify(characteristic, true)
                            }
                        }
                        ConnectionState.CONNECTING -> {
                        }
                    }
                }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable?.dispose()
    }
}