package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sample.ui.main.clients.custom.slot.operatorviews.ResourceView
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import kotlinx.android.synthetic.main.view_characteristic_read.view.*


class ResourceReadView : CardView, ResourceView {

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
        buttonCharRead.setOnClickListener {
            client.read(slot.id()).subscribe()
        }
    }
}

