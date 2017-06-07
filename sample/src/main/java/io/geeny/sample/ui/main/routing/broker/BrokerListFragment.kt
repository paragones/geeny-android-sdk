package io.geeny.sample.ui.main.routing.broker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sample.ui.common.views.LabeledTextView
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.RouteInfo
import kotlinx.android.synthetic.main.fragment_broker.*

class BrokerListFragment : BaseFragment(), BrokerView {

    lateinit var presenter: BrokerListPresenter
    lateinit var adapter: TopicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BrokerListPresenter(sdk().routing.router, sdk().routing.broker, component().ioScheduler, component().mainScheduler)
        adapter = TopicsAdapter()
    }

    override fun layout(): Int = R.layout.fragment_broker

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewTopics.hasFixedSize()
        recyclerViewTopics.layoutManager = LinearLayoutManager(context)
        recyclerViewTopics.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        setTitle("Broker Topics")
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun onTopicsLoaded(infos: List<Pair<TopicInfo, List<RouteInfo>>>) {
        adapter.data = infos
    }

    companion object {
        val TAG = BrokerListFragment::class.java.simpleName
        fun newInstance(): Fragment? = BrokerListFragment()
    }
}


class TopicsAdapter : RecyclerView.Adapter<TopicsAdapter.TopicsViewHolder>() {
    override fun onBindViewHolder(holder: TopicsViewHolder?, position: Int) {
        holder?.bind(data!![position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TopicsViewHolder = TopicsViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.row_topic, parent, false))


    override fun getItemCount(): Int = data?.size ?: 0

    var data: List<Pair<TopicInfo, List<RouteInfo>>>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class TopicsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Pair<TopicInfo, List<RouteInfo>>) {

            (itemView.findViewById<View>(R.id.labeledTextViewTopic) as LabeledTextView).content = item.first.id
            val producer = StringBuilder()
            val consumer = StringBuilder()
            for ((_, direction, _, clientIdentifier, clientResourceId) in item.second) {
                when (direction) {
                    Direction.CONSUMER -> consumer
                    Direction.PRODUCER -> producer
                }.append(clientIdentifier).append(" ").append(clientResourceId).append("\n")
            }

            (itemView.findViewById<View>(R.id.labeledTextViewTopicConsumer) as LabeledTextView).content = consumer.toString()
            (itemView.findViewById<View>(R.id.labeledTextViewTopicProducer) as LabeledTextView).content = producer.toString()
            (itemView.findViewById<View>(R.id.labeledTextViewTopicJournal) as LabeledTextView).content = item.first.topicJournalType.toString()
        }
    }
}
