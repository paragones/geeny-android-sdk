package io.geeny.sample.ui.main.clients.custom.list

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.AppClient
import kotlinx.android.synthetic.main.row_custom.view.*

class CustomClientListRow : CardView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {}

    fun bindConnection(client: Client) {
        textViewCustomClientAddress.text = client.address()
    }
}