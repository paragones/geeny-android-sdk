package com.leondroid.demo_app.ui.main.virtualthing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.leondroid.demo_app.R
import com.leondroid.demo_app.ui.main.blething.GeenyFlowView
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.VirtualThingGateway
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.geeny.flow.GeenyFlow
import io.geeny.sdk.geeny.things.LocalThingInfo
import io.geeny.sdk.routing.router.types.Route
import kotlinx.android.synthetic.main.fragment_thing.*

class VirtualThingFragment : BaseFragment(), VirtualThingGateway.Callback {
    override fun onLocalThingInfoLoad(deviceInfo: LocalThingInfo) {
        textViewThingProtocol.text = "Geeny-Native CloudThingInfo, Protocol v${deviceInfo.protocolVersion}"
        textViewThingTypeValue.text = deviceInfo.thingTypeId
        textViewThingSerialNumber.text = deviceInfo.serialNumber
        textViewThingSerialNumberValue.text = deviceInfo.serialNumber
        textViewThingSerialNumber.visibility = View.VISIBLE

        textViewThingName.text = deviceInfo.deviceName
    }

    private lateinit var gateway: VirtualThingGateway
    private var flowViewMap: MutableMap<GeenyFlow, GeenyFlowView> = HashMap()

    override fun layout(): Int = R.layout.fragment_virtual_thing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gateway = sdk().getVirtualGateway(arguments.getString(ARG_ADDRESS), app().mainScheduler)
    }

    override fun onStart() {
        super.onStart()
        setTitle("Virtual Thing")
        gateway.attach(this)

    }

    override fun onStop() {
        gateway.detach()
        super.onStop()
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
        val TAG = VirtualThingFragment::class.java.simpleName
        val ARG_ADDRESS = "ARG_ADDRESS"

        fun newInstance(address: String): VirtualThingFragment {
            val bundle = Bundle()
            bundle.putString(ARG_ADDRESS, address)

            return VirtualThingFragment().apply {
                arguments = bundle
            }
        }
    }
}