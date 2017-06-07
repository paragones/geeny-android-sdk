package io.geeny.sample.ui.main.routing.router.detail

import android.os.Bundle
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.routing.router.types.Route
import kotlinx.android.synthetic.main.fragment_router_detail.*

class RouterDetailFragment : BaseFragment(), RouterDetailView {

    override fun layout(): Int = R.layout.fragment_router_detail

    lateinit var presenter: RouterDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = RouterDetailPresenter(id(), sdk().routing.router, component().ioScheduler, component().mainScheduler)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun routeLoaded(route: Route) {

        labeledViewRouteDetailTopic.content = route.info().topic
        labeledViewRouteDetailType.content = route.info().type.toString() + " " + route.info().direction.toString()
        labeledViewRouteDetailId.content = route.identifier()
        labeledViewRouteDetailClientId.content = route.info().clientIdentifier
        labeledViewRouteDetailClientResourceId.content = route.info().clientResourceId
        buttonDeleteRouteDetail.setOnClickListener {
            presenter?.delete(route)
        }
    }


    override fun onRunningStateHasChanged(state: ConnectionState) {


        when (state) {
            ConnectionState.CONNECTED -> {
                buttonStartStopRouteDetail.text = "Stop"
                buttonStartStopRouteDetail.setOnClickListener {
                    presenter.stop()
                }
            }
            ConnectionState.DISCONNECTED -> {
                buttonStartStopRouteDetail.text = "Start"
                buttonStartStopRouteDetail.setOnClickListener {
                    presenter.start(context)
                }
            }
            ConnectionState.CONNECTING -> {
                buttonStartStopRouteDetail.text = "Connecting"
                buttonStartStopRouteDetail.setOnClickListener {}
            }
        }
    }

    private fun id(): String = arguments.getString(ARG_ID)

    companion object {
        val TAG = RouterDetailFragment::class.java.simpleName
        val ARG_ID = "ARG_ID"

        fun newInstance(id: String): RouterDetailFragment {
            val bundle = Bundle()
            bundle.putString(ARG_ID, id)

            return RouterDetailFragment().apply { arguments = bundle }
        }
    }
}