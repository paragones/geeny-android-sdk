package io.geeny.sample.ui.main.clients.ble.characterstics.views.operatorviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.custom.slot.operatorviews.ResourceView
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.clients.custom.slots.ResourceAccessType

class ResourceLayout : ScrollView, ResourceView {

    private var container: LinearLayout? = null

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
        container = LinearLayout(context)
        container?.orientation = LinearLayout.VERTICAL
        addView(container)
    }

    override fun bind(client: Client, slot: Slot) {
        slot.types().map { instantiateItem(client, slot, it) }.forEach {
            container?.addView(it)
        }
    }

    private fun instantiateItem(client: Client, slot: Slot, accessType: ResourceAccessType): android.view.View {
        val viewId: Int = when (accessType) {
            ResourceAccessType.WRITE -> R.layout.view_control_write
            ResourceAccessType.READ -> R.layout.view_control_read
            ResourceAccessType.NOTIFY -> R.layout.view_control_notify
        }

        val view = LayoutInflater.from(container!!.context).inflate(viewId, container, false)
        (view as ResourceView).bind(client, slot)

        return view
    }
}