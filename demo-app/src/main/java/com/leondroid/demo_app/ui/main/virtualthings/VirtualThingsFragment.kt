package com.leondroid.demo_app.ui.main.virtualthings

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.leondroid.demo_app.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.common.Client
import kotlinx.android.synthetic.main.fragment_things.*

class VirtualThingsFragment : BaseFragment(), VirtualThingsAdapter.Callback, VirtualThingsView {

    private lateinit var presenter: VirtualThingsPresenter
    private lateinit var adapter: VirtualThingsAdapter

    override fun layout(): Int = R.layout.fragment_things

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = VirtualThingsPresenter(sdk(), app().ioScheduler, app().mainScheduler)
        adapter = VirtualThingsAdapter()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewThings.hasFixedSize()
        recyclerViewThings.layoutManager = LinearLayoutManager(context)
        recyclerViewThings.adapter = adapter
    }

    override fun onStart() {
        setTitle("Things")
        adapter.callback = this
        presenter.attach(this)
        presenter.load()
        super.onStart()
    }

    override fun onStop() {
        adapter.callback = null
        super.onStop()
    }

    override fun onThingsLoaded(clients: List<Client>) {
        adapter.data = clients
    }

    override fun onVirtualThingClicked(client: Client) {
        (activity as Container).onVirtualThingClicked(client.address())
    }

    interface Container {
        fun onVirtualThingClicked(address: String)
    }

    companion object {
        val TAG = VirtualThingsFragment::class.java.simpleName
    }
}

class VirtualThingsAdapter : RecyclerView.Adapter<VirtualThingsAdapter.VirtualThingsViewHolder>() {
    var data: List<Client>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var callback: Callback? = null

    override fun onBindViewHolder(holder: VirtualThingsViewHolder?, position: Int) {
        holder?.bind(data!![position])
        holder?.row?.setOnClickListener {
            callback?.onVirtualThingClicked(data!![position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VirtualThingsViewHolder {
        val row = LayoutInflater.from(parent?.context).inflate(R.layout.row_virutal_things, parent, false) as VirtualThingsRow
        return VirtualThingsViewHolder(row)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class VirtualThingsViewHolder(val row: VirtualThingsRow) : RecyclerView.ViewHolder(row) {
        fun bind(client: Client) {
            row.bind(client)
        }
    }

    interface Callback {
        fun onVirtualThingClicked(client: Client)
    }
}