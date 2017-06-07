package io.geeny.sample.ui.main.clients.custom.detail

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.ConnectionState
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.row_custom_client_resource.view.*

class CustomClientResourceRow : CardView {
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

    fun bind(slot: Slot) {
        disposable?.dispose()
        textViewResourceName.text = slot.name()
        textViewResourceType.text = slot.type().toString()

        disposable = slot.state()
                .subscribe {
                    when (it) {
                        ConnectionState.CONNECTED -> {
                            textViewResourceConnectionStatus.text = "Connected"
                            textViewResourceConnectionStatus.setTextColor(Color.GREEN)
                        }
                        ConnectionState.DISCONNECTED -> {
                            textViewResourceConnectionStatus.text = "Disconnected"
                            textViewResourceConnectionStatus.setTextColor(Color.RED)
                        }
                        ConnectionState.CONNECTING -> {
                            textViewResourceConnectionStatus.text = "Connecting..."
                            textViewResourceConnectionStatus.setTextColor(Color.GRAY)
                        }
                    }
                }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable?.dispose()
    }
}