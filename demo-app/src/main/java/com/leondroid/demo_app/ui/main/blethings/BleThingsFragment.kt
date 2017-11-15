package com.leondroid.demo_app.ui.main.blethings

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.leondroid.demo_app.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.ble.BleClient
import kotlinx.android.synthetic.main.fragment_things.*

class BleThingsFragment : BaseFragment(), ThingsAdapter.Callback, ThingsView {

    private lateinit var presenter: ThingsPresenter
    private lateinit var adapter: ThingsAdapter

    override fun layout(): Int = R.layout.fragment_things

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ThingsPresenter(sdk(), app().ioScheduler, app().mainScheduler)
        adapter = ThingsAdapter()
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

    override fun onThingsLoaded(clients: List<BleClient>) {
        adapter.data = clients
    }

    override fun onThingClicked(client: BleClient) {
        if(client.isGeenyDevice()) {
            (activity as Container).onThingClicked(client.address())
        } else {
            toast("Can't open a non geeny device")
        }
    }

    interface Container {
        fun onThingClicked(address: String)
    }

    companion object {
        val TAG = BleThingsFragment::class.java.simpleName
    }
}

class ThingsAdapter : RecyclerView.Adapter<ThingsAdapter.ThingsViewHolder>() {
    var data: List<BleClient>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var callback: Callback? = null

    override fun onBindViewHolder(holder: ThingsViewHolder?, position: Int) {
        holder?.bind(data!![position])
        holder?.row?.setOnClickListener {
            callback?.onThingClicked(data!![position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ThingsViewHolder {
        val row = LayoutInflater.from(parent?.context).inflate(R.layout.row_things, parent, false) as ThingsRow
        return ThingsViewHolder(row)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class ThingsViewHolder(val row: ThingsRow) : RecyclerView.ViewHolder(row) {
        fun bind(client: BleClient) {
            row.bind(client)
        }
    }

    interface Callback {
        fun onThingClicked(client: BleClient)
    }
}