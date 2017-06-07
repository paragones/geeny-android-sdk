package io.geeny.sample.ui.main.clients.common.routing.consume

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo

class RoutingCreateConsumerLayout : ScrollView, TopicView {

    lateinit var container: LinearLayout
    lateinit var presenter: TopicPresenter
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
        container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        //val params: ViewGroup.LayoutParams = ScrollView.LayoutPa
        addView(container)
        val component = GatewayApp.from(context).component
        presenter = TopicPresenter(component.sdk, component.ioScheduler, component.mainScheduler)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.attach(this)
        presenter.load()
    }

    override fun onDetachedFromWindow() {
        presenter.detach()
        super.onDetachedFromWindow()
    }


    override fun showTopics(topics: List<TopicInfo>) {
        container.removeAllViews()

        if (topics.isEmpty()) {
            val errorView = TextView(context)
            errorView.text = "No topics created yet"
            errorView.gravity = Gravity.CENTER
            errorView.setTextColor(Color.BLACK)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(16, 16, 16, 16)
            container.addView(errorView, params)
        } else {
            for (topic in topics) {
                addTopicView(topic)
            }
        }
    }

    private fun addTopicView(topic: TopicInfo) {
        val topicView = LayoutInflater.from(context).inflate(R.layout.view_topic, container, false) as TextView
        topicView.text = topic.id
        container.addView(topicView)

        topicView.setOnClickListener {
            onTopicSelected(topic)
        }
    }

    private fun onTopicSelected(topic: TopicInfo) {
        callback?.onTopicSelected(topic)
    }

    interface Callback {
        fun onTopicSelected(topic: TopicInfo)
    }
}