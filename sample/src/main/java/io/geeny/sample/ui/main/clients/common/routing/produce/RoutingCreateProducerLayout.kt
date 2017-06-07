package io.geeny.sample.ui.main.clients.common.routing.produce

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.geeny.sample.R
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import kotlinx.android.synthetic.main.layout_routing_producer.view.*

class RoutingCreateProducerLayout : LinearLayout {

    var callback: Callback? = null

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
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_routing_producer, this, true)
        buttonCreateProduceRouteDismissOnRead.setOnClickListener {
            callback?.createProducerRoute(TopicJournalType.DISMISS_ON_READ)
        }

        buttonCreateProduceRouteFanOut.setOnClickListener {
            callback?.createProducerRoute(TopicJournalType.FAN_OUT)
        }
        buttonCreateProduceRouteOverwrite.setOnClickListener {
            callback?.createProducerRoute(TopicJournalType.OVERWRITE)
        }

    }

    interface Callback {
        fun createProducerRoute(topicJournalType: TopicJournalType)
    }
}