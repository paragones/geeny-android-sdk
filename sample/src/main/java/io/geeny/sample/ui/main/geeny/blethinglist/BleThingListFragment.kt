package io.geeny.sample.ui.main.geeny.blethinglist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.geeny.cloud.api.repos.DeviceInfo
import kotlinx.android.synthetic.main.fragment_ble_thing_list.*

class BleThingListFragment : BaseFragment(), BleThingListView {

    override fun layout(): Int = R.layout.fragment_ble_thing_list
    private lateinit var presenter: BleThingListPresenter
    private lateinit var adapter: BleThingAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BleThingListPresenter(sdk(), app().component.ioScheduler, app().component.mainScheduler)
        adapter = BleThingAdapter()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewBleThingList.hasFixedSize()
        recyclerViewBleThingList.layoutManager = LinearLayoutManager(context)
        recyclerViewBleThingList.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        setTitle("Registered things")
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun showList(list: List<DeviceInfo>) {
        adapter.data = list
    }

    companion object {
        val TAG = BleThingListFragment::class.java.simpleName

        fun newInstance() : BleThingListFragment = BleThingListFragment()
    }
}

class BleThingAdapter : RecyclerView.Adapter<BleThingAdapter.BleThingViewHolder>() {
    var data: List<DeviceInfo>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: BleThingViewHolder?, position: Int) {
        holder?.bind(data!![position])
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BleThingViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_ble_thing, parent, false) as BleThingRow
        return BleThingViewHolder(view)
    }


    class BleThingViewHolder(val bleThingRow: BleThingRow) : RecyclerView.ViewHolder(bleThingRow) {
        fun bind(deviceInfo: DeviceInfo) {
            bleThingRow.bind(deviceInfo)
        }

    }
}