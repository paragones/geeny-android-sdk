package com.leondroid.demo_app.ui.main.thing

import android.os.Bundle
import android.view.LayoutInflater
import com.leondroid.demo_app.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.common.ConnectionState
import kotlinx.android.synthetic.main.fragment_thing.*
import android.view.View
import io.geeny.sdk.BleGateway
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.routing.router.types.Route

class ThingFragment : BaseFragment(), BleGateway.Callback {


    private lateinit var gateway: BleGateway
    private var flowViewMap: MutableMap<GeenyFlow, GeenyFlowView> = HashMap()

    override fun layout(): Int = R.layout.fragment_thing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gateway = sdk().getBleGateway(arguments.getString(ARG_ADDRESS), app().mainScheduler)
    }

    override fun onStart() {
        super.onStart()
        setTitle("Thing Info")
        gateway.attach(this)

    }

    override fun onStop() {
        gateway.detach()
        super.onStop()
    }

    override fun onClientLoaded(client: BleClient) {
        textViewThingName.text = client.name()
        gateway.connect(context)
    }


    override fun onConnectionStateHasChanged(connectionState: ConnectionState) {
        buttonThingConnect.visibility = View.VISIBLE
        buttonThingConnect.text = when (connectionState) {
            ConnectionState.CONNECTED -> "Disconnect"
            ConnectionState.DISCONNECTED -> "Connect"
            ConnectionState.CONNECTING -> "Connecting..."
        }

        buttonThingConnect.setOnClickListener {
            when (connectionState) {
                ConnectionState.CONNECTED -> gateway.disconnect()
                ConnectionState.DISCONNECTED -> gateway.connect(context)
                ConnectionState.CONNECTING -> TODO()
            }
        }
    }

    override fun onDeviceInfoLoad(deviceInfo: DeviceInfo) {
        textViewThingProtocol.text = "Geeny-Native Thing, Protocol v${deviceInfo.protocolVersion}"
        textViewThingTypeValue.text = deviceInfo.thingTypeId.toString()
        textViewThingSerialNumber.text = deviceInfo.serialNumber.toString()
        textViewThingSerialNumberValue.text = deviceInfo.serialNumber.toString()
        textViewThingSerialNumber.visibility = View.VISIBLE
    }

    override fun onDeviceIsNotRegisteredYet() {
        buttonRegisterUnregisterThing.visibility = View.VISIBLE
        buttonRegisterUnregisterThing.setOnClickListener{
            gateway.register()
        }
    }

    override fun onFlowsLoaded(flows: List<GeenyFlow>) {
        buttonRegisterUnregisterThing.visibility = View.GONE

        for (flow in flows) {
            val view = LayoutInflater.from(activity).inflate(R.layout.row_geeny_flow, containerGeenyFlow, false) as GeenyFlowView
            flowViewMap[flow] = view
            view.bind(flow, object: GeenyFlowView.Callback {
                override fun start(flow: GeenyFlow) {
                    gateway.start(flow, activity)
                }

                override fun read(resourceId: String) {
                    gateway.triggerRead(resourceId)
                }

            })
            containerGeenyFlow.addView(view)
        }
    }

    override fun onError(throwable: Throwable) {
        showError()(throwable)
    }

    override fun onValueHasChanged(flow: GeenyFlow, bytes: ByteArray) {
        flowViewMap[flow]?.setCurrentValue(bytes)
    }

    override fun onRouteConnectionStatusHasChanged(flow: GeenyFlow, route: Route, status: ConnectionState) {
        flowViewMap[flow]?.onConnectionStatusHasChanged(route, status)
    }

    override fun progressView(): View? = progressThing

    companion object {
        val TAG = ThingFragment::class.java.simpleName
        val ARG_ADDRESS = "ARG_ADDRESS"

        fun newInstance(address: String): ThingFragment {
            val bundle = Bundle()
            bundle.putString(ARG_ADDRESS, address)

            return ThingFragment().apply {
                arguments = bundle
            }
        }
    }
}