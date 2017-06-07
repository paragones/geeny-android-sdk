package io.geeny.sample.ui.main.clients.custom.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.custom.AppClient
import kotlinx.android.synthetic.main.fragment_custom_client_list.*

class CustomClientListFragment : BaseFragment(), CustomClientListView, CustomClientListAdapter.Callback {


    lateinit var presenter: CustomClientListPresenter
    lateinit var adapter: CustomClientListAdapter
    override fun layout(): Int = R.layout.fragment_custom_client_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CustomClientListPresenter(sdk(), component().ioScheduler, component().mainScheduler)
        adapter = CustomClientListAdapter()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCustomClientList.hasFixedSize()
        recyclerViewCustomClientList.layoutManager = LinearLayoutManager(context)
        recyclerViewCustomClientList.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.load()
        adapter.callback = this
        setTitle("Custom Clients")
    }

    override fun onStop() {
        adapter.callback = null
        presenter.detach()
        super.onStop()
    }


    override fun showClients(clients: List<AppClient>) {
        adapter.data = clients
    }

    override fun onCustomClientSelected(address: String) {
        (activity as Container).openCustomClient(address)
    }

    interface Container {
        fun openCustomClient(address: String)
    }

    companion object {
        val TAG = CustomClientListFragment::class.java.simpleName
        fun newInstance(): CustomClientListFragment {
            return CustomClientListFragment()
        }
    }
}


class CustomClientListAdapter : RecyclerView.Adapter<CustomClientListAdapter.ViewHolder>() {

    var callback: Callback? = null

    override fun onBindViewHolder(viewHolder: ViewHolder?, position: Int) {
        viewHolder!!.bind(data!![position])
        viewHolder.itemView.setOnClickListener {
            if (callback != null) {
                callback!!.onCustomClientSelected(data!![position].address())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.row_custom, parent, false) as CustomClientListRow)

    override fun getItemCount(): Int = data?.size ?: 0

    var data: List<AppClient>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(private val row: CustomClientListRow) : RecyclerView.ViewHolder(row) {
        fun bind(client: AppClient) {
            row.bindConnection(client)
        }
    }

    interface Callback {
        fun onCustomClientSelected(address: String)
    }
}