package io.geeny.sample.ui.main.clients.custom.detail

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot
import kotlinx.android.synthetic.main.fragment_custom_client_detail.*

class CustomClientDetailFragment : BaseFragment(), CustomClientDetailView, ResourcePagerAdapter.Callback {
    private lateinit var adapter: ResourcePagerAdapter
    private lateinit var presenter: CustomClientDetailPresenter
    override fun layout(): Int = R.layout.fragment_custom_client_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CustomClientDetailPresenter(arguments.getString(ARG_ADDRESS), sdk(), component().ioScheduler, component().mainScheduler)
        adapter = ResourcePagerAdapter()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCustomClientDetail.hasFixedSize()
        recyclerViewCustomClientDetail.layoutManager = LinearLayoutManager(context)
        recyclerViewCustomClientDetail.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        adapter.callback = this
    }

    override fun onStop() {
        adapter.callback = null
        presenter.detach()
        super.onStop()
    }

    override fun showClient(customClient: Client, slotList: List<Slot>) {
        setTitle(customClient.address())
        adapter.data = slotList
    }

    override fun onResourceSelected(slot: Slot) {
        (activity as Container).openCustomResource(arguments.getString(ARG_ADDRESS), slot.id())
    }

    companion object {
        val TAG = CustomClientDetailFragment::class.java.simpleName
        val ARG_ADDRESS = "ARG_ADDRESS"
        fun newInstance(address: String): CustomClientDetailFragment {
            val arg = Bundle()
            arg.putString(ARG_ADDRESS, address)
            return CustomClientDetailFragment().apply {
                arguments = arg
            }
        }
    }

    interface Container {
        fun openCustomResource(clientId: String, resourceId: String)
    }
}


class ResourcePagerAdapter() : RecyclerView.Adapter<ResourcePagerAdapter.CustomClientDetailVH>() {
    override fun onBindViewHolder(holder: CustomClientDetailVH?, position: Int) {
        val resource = data!![position]
        holder?.bind(resource)
        holder?.itemView?.setOnClickListener {
            callback?.onResourceSelected(resource)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomClientDetailVH {
        return CustomClientDetailVH((LayoutInflater.from(parent?.context).inflate(R.layout.row_custom_client_resource, parent, false) as CustomClientResourceRow))
    }

    override fun getItemCount(): Int = data?.size ?: 0


    var data: List<Slot>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var callback: Callback? = null

    class CustomClientDetailVH(val row: CustomClientResourceRow) : RecyclerView.ViewHolder(row) {
        fun bind(slot: Slot) {
            row.bind(slot)
        }
    }

    interface Callback {
        fun onResourceSelected(slot: Slot)
    }
}
