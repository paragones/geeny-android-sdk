package io.geeny.sample.ui.main.clients.common.routing

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.LinearLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.characterstics.views.ConnectionView
import io.geeny.sample.ui.main.clients.common.routing.consume.RoutingCreateConsumerLayout
import io.geeny.sample.ui.main.clients.common.routing.produce.RoutingCreateProducerLayout
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.Route
import io.geeny.sdk.routing.router.types.RouteType
import kotlinx.android.synthetic.main.view_routing.view.*
import io.geeny.sdk.clients.custom.slots.ResourceType
import io.geeny.sdk.clients.mqtt.GeenyMqttClient
import io.geeny.sdk.routing.bote.topicjournal.TopicInfo
import io.geeny.sdk.routing.bote.topicjournal.TopicJournalType
import io.reactivex.disposables.CompositeDisposable


class RoutingLayout : LinearLayout, ConnectionView, RoutingView, RoutingCreateConsumerLayout.Callback, RoutingCreateProducerLayout.Callback {
    private var presenter: RoutingPresenter? = null

    override fun hideProducer() {
        containerRoutingProducer.visibility = ViewGroup.GONE
        routingDivider.visibility = ViewGroup.GONE
    }

    override fun hideConsumer() {
        containerRoutingConsumer.visibility = ViewGroup.GONE
        routingDivider.visibility = ViewGroup.GONE
    }

    private var consumerExtrasShown: Boolean = false
    private var producerExtrasShown: Boolean = false

    override fun noRouteRegisteredYet(emptyRoute: Route) {
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
        when (emptyRoute.info().direction) {
            Direction.CONSUMER -> {
                textViewConsumerRouteStatus.text = "no consumer route"
                showView(routingControlLayoutConsumer, false)
                showView(routingCreateConsumerLayout, consumerExtrasShown)

                showView(routingControlLayoutConsumer, false)
                textViewConsumerRouteStatus.setOnClickListener {
                    consumerExtrasShown = !consumerExtrasShown
                    showView(routingCreateConsumerLayout, consumerExtrasShown, true)
                }
            }
            Direction.PRODUCER -> {
                textViewProducerRouteStatus.text = "no producer route"
                showView(routingControlLayoutProducer, false)
                showView(routingCreateProducerLayout, producerExtrasShown)

                textViewProducerRouteStatus.setOnClickListener {
                    producerExtrasShown = !producerExtrasShown
                    showView(routingCreateProducerLayout, producerExtrasShown, true)
                }
            }
        }
    }

    private var compositeDisposable: CompositeDisposable? = null

    override fun onRouteLoaded(route: Route) {
        when (route.info().direction) {
            Direction.CONSUMER -> {
                textViewConsumerRouteStatus.text = "Consumer: " + route.info().topic
                showView(routingCreateConsumerLayout, false)
                showView(routingControlLayoutConsumer, consumerExtrasShown)

                textViewConsumerRouteStatus.setOnClickListener {
                    consumerExtrasShown = !consumerExtrasShown
                    showView(routingControlLayoutConsumer, consumerExtrasShown, true)
                }
                routingControlLayoutConsumer.setIsStarted(route.isStarted())
            }
            Direction.PRODUCER -> {
                textViewProducerRouteStatus.text = "Producer: " + route.info().topic
                showView(routingCreateProducerLayout, false)
                showView(routingControlLayoutProducer, producerExtrasShown)

                textViewProducerRouteStatus.setOnClickListener {
                    producerExtrasShown = !producerExtrasShown
                    showView(routingControlLayoutProducer, producerExtrasShown, true)
                }
                routingControlLayoutProducer.setIsStarted(route.isStarted())
            }
        }
    }

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
        LayoutInflater.from(context).inflate(R.layout.view_routing, this, true)
        orientation = VERTICAL
        textViewConsumerRouteStatus.isSelected = true
        textViewProducerRouteStatus.isSelected = true

        routingCreateProducerLayout.callback = this
        routingCreateConsumerLayout.callback = this

        routingControlLayoutProducer.callback = object : RoutingControlLayout.Callback {
            override fun onStartRoute() {
                presenter?.start(Direction.PRODUCER, context)
            }

            override fun onStopRoute() {
                presenter?.stop(Direction.PRODUCER)
            }

            override fun onDeleteRoute() {
                presenter?.delete(Direction.PRODUCER)
            }
        }

        routingControlLayoutConsumer.callback = object : RoutingControlLayout.Callback {
            override fun onStartRoute() {
                presenter?.start(Direction.CONSUMER, context)
            }

            override fun onStopRoute() {
                presenter?.stop(Direction.CONSUMER)
            }

            override fun onDeleteRoute() {
                presenter?.delete(Direction.CONSUMER)
            }
        }
    }

    override fun bind(client: BleClient, characteristic: BluetoothGattCharacteristic) {
        val app = GatewayApp.from(context)
        presenter = RoutingPresenter(
                RouteType.BLE,
                client.address(),
                characteristic.uuid.toString(),
                ResourceType.CHANNEL,
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
        loadData()
    }

    fun bind(client: Client, slot: Slot) {
        val app = GatewayApp.from(context)
        presenter = RoutingPresenter(
                RouteType.CUSTOM,
                client.address(),
                slot.id(),
                slot.type(),
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
        loadData()
    }

    fun bind(client: GeenyMqttClient, mqttTopic: String) {
        presenter?.detach()
        val app = GatewayApp.from(context)
        presenter = RoutingPresenter(
                RouteType.MQTT,
                client.mqttConfig.id(),
                mqttTopic,
                ResourceType.CHANNEL,
                app.component.sdk,
                app.component.ioScheduler,
                app.component.mainScheduler)
        loadData()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadData()
    }

    private fun loadData() {
        presenter?.attach(this)
        presenter?.load()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.detach()
    }

    override fun showError(): (Throwable) -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toast(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun progress(show: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTopicSelected(topic: TopicInfo) {
        presenter?.registerConsumer(topic.id)
    }

    override fun createProducerRoute(topicJournalType: TopicJournalType) {
        presenter?.registerProducer(topicJournalType)
    }


    private fun showView(view: View, show: Boolean, animated: Boolean = false) {
        view.visibility = if (show) ViewGroup.VISIBLE else View.GONE
    }
}

