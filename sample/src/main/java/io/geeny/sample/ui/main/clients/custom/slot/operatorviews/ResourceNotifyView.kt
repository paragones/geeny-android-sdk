package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sample.GatewayApp
import io.geeny.sample.ui.main.clients.custom.slot.operatorviews.ResourceView
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_characteristic_notify.view.*

class ResourceNotifyView : CardView, ResourceView {
    private var disposable: Disposable? = null

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


    override fun bind(client: Client, slot: Slot) {
        val app = GatewayApp.from(context)


        disposable = client.slotState(slot.id())
                .observeOn(app.component.mainScheduler)
                .subscribe { state ->
                    when (state) {
                        ConnectionState.CONNECTED -> {
                            textViewCharConnectionsStatus.text = "Connected"
                            buttonCharNotify.text = "Turn Off"

                            buttonCharNotify.setOnClickListener {
                                client.notify(slot.id(), false)
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            textViewCharConnectionsStatus.text = "Not Connected"
                            buttonCharNotify.text = "Turn On"
                            buttonCharNotify.setOnClickListener {
                                client.notify(slot.id(), true)
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