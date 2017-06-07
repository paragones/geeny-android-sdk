package io.geeny.sample.ui.main.clients.mqtt.detail

import android.os.Bundle
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.routing.router.types.Route
import kotlinx.android.synthetic.main.fragment_mqtt_detail.*

class MqttDetailFragment : BaseFragment(), MqttDetailView {
    lateinit var presenter: MqttDetailPresenter
    fun serverUri(): String = arguments.getString(ARG_MQTT_CONFIG_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = MqttDetailPresenter(serverUri(), component().sdk, component().ioScheduler, component().mainScheduler)
    }

    override fun layout(): Int = R.layout.fragment_mqtt_detail

    override fun onStart() {
        super.onStart()
        setTitle(serverUri())
        presenter.attach(this)
        presenter.load()
        buttonCreateMqttTopic.setOnClickListener{
            if(editTextMqttTopic.text.toString().isNotEmpty()) {
                presenter?.createTopic(editTextMqttTopic.text.toString())
            }
        }
    }

    override fun onStop() {
        presenter.detach()

        buttonCreateMqttTopic.setOnClickListener{}
        super.onStop()
    }

    override fun showClient(mqttClient: GeenyMqttClient) {
        textViewMqttDetailClientId.content = mqttClient.mqttConfig.clientId
        textViewMqttDetailServerUri.content = mqttClient.mqttConfig.serverUri
        mqttConnectionLayout.connect(mqttClient.mqttConfig.id())
        if(mqttClient.certificateInfo != null) {
            textViewHasCertificateLoaded.text = "Certificate is loaded"
        }
    }

    override fun onListLoaded(routes: List<Route>) {

    }

    override fun showMqttTopicForm(client: GeenyMqttClient, mqttTopic: String) {
        routingLayoutForm.bind(client, mqttTopic)
        routingLayoutForm.visibility = ViewGroup.VISIBLE
        textViewTopicFormLabel.text = mqttTopic
    }

    companion object {
        val TAG = MqttDetailFragment::class.java.simpleName
        val ARG_MQTT_CONFIG_ID = "ARG_MQTT_CONFIG_ID"

        fun newInstance(serverUri: String): MqttDetailFragment {
            val fragment = MqttDetailFragment()

            val arg = Bundle()
            arg.putString(ARG_MQTT_CONFIG_ID, serverUri)
            fragment.arguments = arg
            return fragment
        }
    }
}