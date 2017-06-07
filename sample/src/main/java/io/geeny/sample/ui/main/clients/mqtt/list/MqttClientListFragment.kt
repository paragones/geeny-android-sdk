package io.geeny.sample.ui.main.clients.mqtt.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.clients.mqtt.MqttConfig
import kotlinx.android.synthetic.main.fragment_mqtt_client_list.*

class MqttClientListFragment : BaseFragment(), MqttClientListView, MqttListAdapter.Callback {
    lateinit var presenter: MqttClientListPresenter
    lateinit var adapter: MqttListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = GatewayApp.from(activity)
        presenter = MqttClientListPresenter(app.component.sdk, app.component.ioScheduler, app.component.mainScheduler)
        adapter = MqttListAdapter()
    }

    override fun layout(): Int = R.layout.fragment_mqtt_client_list

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewMqttClients.hasFixedSize()
        recyclerViewMqttClients.layoutManager = LinearLayoutManager(context)
        recyclerViewMqttClients.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        adapter.callback = this
        setTitle("Mqtt List")
    }

    override fun onStop() {
        adapter.callback = null
        presenter.detach()
        super.onStop()
    }

    override fun onMqttClientSelected(config: MqttConfig) {
        presenter.onMqttClientSelected(config)
    }

    override fun mqttClientsLoaded(mqttClients: List<GeenyMqttClient>) {
        adapter.data = mqttClients
    }

    override fun openMqttClient(config: MqttConfig) {
        (activity as Container).openMqttClient(config)
    }

    interface Container {
        fun openMqttClient(config: MqttConfig)
    }

    companion object {
        val TAG = MqttClientListFragment::class.java.simpleName
        fun newInstance(): MqttClientListFragment = MqttClientListFragment()
    }
}


class MqttListAdapter : RecyclerView.Adapter<MqttListAdapter.ViewHolder>() {

    var callback: Callback? = null

    override fun onBindViewHolder(viewHolder: MqttListAdapter.ViewHolder?, position: Int) {
        viewHolder!!.bind(data!![position])
        viewHolder.itemView.setOnClickListener {
            if (callback != null) {
                callback!!.onMqttClientSelected(data!![position].mqttConfig)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MqttListAdapter.ViewHolder = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.row_mqtt, parent, false) as MqttClientListRow)

    override fun getItemCount(): Int = data?.size ?: 0

    var data: List<GeenyMqttClient>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(private val row: MqttClientListRow) : RecyclerView.ViewHolder(row) {
        fun bind(client: GeenyMqttClient) {
            row.bindConnection(client)
        }
    }

    interface Callback {
        fun onMqttClientSelected(config: MqttConfig)
    }
}
