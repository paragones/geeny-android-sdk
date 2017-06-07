package io.geeny.sample.ui.main.clients.custom.slot

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.ui.main.clients.custom.slot.operatorviews.ResourceView
import io.geeny.sdk.clients.ble.GattResult
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.common.TypeConverters
import io.geeny.sdk.common.toHex
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.view_custom_client_resource.view.*
import java.util.*

class CustomClientResourceView : LinearLayout, ResourceView {


    private var compositeDisposable: CompositeDisposable? = null

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


    private var client: Client? = null
    private var slot: Slot? = null

    override fun bind(client: Client, slot: Slot) {
        this.client = client
        this.slot = slot
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (client != null) {
            setValues(client!!, slot!!)
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (client != null) {
            setValues(client!!, slot!!)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable?.dispose()
    }

    fun setValues(client: Client, slot: Slot) {
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()

        labeledResourceClientIdentifier.content = client.address()
        labelResourceIdentifier.content = slot.id()

        resourceLayout.bind(client, slot)
        routingLayoutCustom.bind(client, slot)

        compositeDisposable?.add(
                client.value(slot.id())
                        .observeOn(GatewayApp.from(context).component.mainScheduler)
                        .subscribe {
                            labeledResourceCurrentValue.content = it.toHex() + " ("+TypeConverters.bytesToInt(it) + ")"
                            labeledResourceLastUpdate.content = GattResult.dt.format(Date()).toString()
                        }
        )
    }
}