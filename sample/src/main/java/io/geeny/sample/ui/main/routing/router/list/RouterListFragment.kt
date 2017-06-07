package io.geeny.sample.ui.main.routing.router.list

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sample.ui.common.views.StartStopButton
import io.geeny.sdk.common.ConnectionState
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.Route
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_router.*
import kotlinx.android.synthetic.main.row_custom_client_resource.view.*
import kotlinx.android.synthetic.main.row_route.view.*

class RouterListFragment : BaseFragment(), RouterListView, RoutingAdpater.RoutingCallback {
    private lateinit var presenter: RouterListPresenter
    private lateinit var adapter: RoutingAdpater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = GatewayApp.from(activity)

        presenter = RouterListPresenter(
                app.component.sdk.routing.router,
                app.component.ioScheduler,
                app.component.mainScheduler)

        adapter = RoutingAdpater()
    }

    override fun layout(): Int = R.layout.fragment_router

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewRoutes.hasFixedSize()
        recyclerViewRoutes.layoutManager = LinearLayoutManager(context)
        recyclerViewRoutes.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        adapter.callback = this
        setTitle("Router")

        buttonDeleteAllRoutes.setOnClickListener {
            presenter.deleteAll()
        }

        buttonStartAllRoutes.setOnClickListener {
            presenter.startAll(context)
        }
    }

    override fun onStop() {
        buttonDeleteAllRoutes.setOnClickListener {}
        buttonStartAllRoutes.setOnClickListener {}
        adapter.callback = null
        presenter.detach()
        super.onStop()
    }


    override fun showRoutes(routes: List<Route>) {
        adapter?.data = routes
    }

    override fun onRouteClicked(route: Route) {
        presenter?.openRoute(route)
    }

    override fun openRoute(route: Route) {
        (activity as Container).openRoute(route)
    }

    interface Container {
        fun openRoute(route: Route)
    }

    override fun onDeletedAllRoutes() {
        adapter.data = null
    }

    companion object {
        val TAG = RouterListFragment::class.java.simpleName
        fun newInstance(): Fragment? = RouterListFragment()

    }
}

class RoutingAdpater() : RecyclerView.Adapter<RoutingAdpater.VH>() {

    var callback: RoutingCallback? = null

    var data: List<Route>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder?.bind(data!![position], object : RoutingCallback {
            override fun onRouteClicked(route: Route) {
                callback?.onRouteClicked(route)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH = VH(LayoutInflater.from(parent?.context).inflate(R.layout.row_route, parent, false) as RouteView)

    override fun getItemCount(): Int = data?.size ?: 0

    class VH(val view: RouteView) : RecyclerView.ViewHolder(view) {
        fun bind(route: Route, callback: RoutingCallback) {
            view.bind(route, callback)
        }
    }

    interface RoutingCallback {
        fun onRouteClicked(route: Route)
    }
}

class RouteView : CardView {

    var disposable: Disposable? = null

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
    }

    fun bind(route: Route, callback: RoutingAdpater.RoutingCallback) {
        disposable?.dispose()
        when (route.info().direction) {
            Direction.CONSUMER -> {
                labeledViewRouteFrom.content = route.info().topic
                labeledViewRouteTo.content = route.info().clientResourceId
            }
            Direction.PRODUCER -> {
                labeledViewRouteFrom.content = route.info().clientResourceId
                labeledViewRouteTo.content = route.info().topic
            }
        }
        labeledViewRouteType.content = route.info().type.toString() + " " + route.info().direction

        buttonRouteStartStop.callback = object : StartStopButton.Callback {
            override fun onStart() {
                route.start(context)
            }

            override fun onStop() {
                route.stop()
            }
        }

        buttonRouteStartStop.connect(route.running())
        setOnClickListener { callback.onRouteClicked(route) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable?.dispose()
    }

}
