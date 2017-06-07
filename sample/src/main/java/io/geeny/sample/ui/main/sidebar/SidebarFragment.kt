package io.geeny.sample.ui.main.sidebar

import android.os.Bundle
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import kotlinx.android.synthetic.main.fragment_sidebar.*
import kotlinx.android.synthetic.main.sidebar_item.view.*
import kotlinx.android.synthetic.main.sidebar_label.view.*
import java.util.*

class SidebarFragment : BaseFragment(), SidebarAdapter.Callback {
    override fun layout(): Int = R.layout.fragment_sidebar

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewSidebar.hasFixedSize()
        recyclerViewSidebar.layoutManager = LinearLayoutManager(context)
        recyclerViewSidebar.adapter = SidebarAdapter(DEFAULT_SIDEBAR, this)
    }

    override fun onSidebarItemClicked(item: SidebarItem) {
        (activity as Container).onSidebarItemClicked(item)
    }

    companion object {
        val DEFAULT_SIDEBAR: Sidebar =
                Sidebar(listOf(
                        SidebarGroup(R.string.label_geeny_profile, listOf(
                                SidebarItem.GEENY_PROFILE
                        )),
                        SidebarGroup(R.string.label_cp, listOf(
                                SidebarItem.BLE_LIST,
                                SidebarItem.MQTT,
                                SidebarItem.CUSTOM_CLIENTS
                        )),
                        SidebarGroup(R.string.label_routing, listOf(
                                SidebarItem.BROKER,
                                SidebarItem.ROUTING
                        )),
                        SidebarGroup(R.string.app_page, listOf(
                                SidebarItem.CHART,
                                SidebarItem.DUMP
                        ))
                ))
        val TAG: String? = SidebarFragment::class.java.simpleName
    }

    interface Container {
        fun onSidebarItemClicked(item: SidebarItem)
    }
}

class SidebarAdapter(private val sidebar: Sidebar, val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val types: MutableMap<Int, Int> = HashMap()
    private val items: MutableMap<Int, SidebarItem> = HashMap()
    private val labels: MutableMap<Int, Int> = HashMap()

    init {
        var current = 0

        for (group in sidebar.groups) {
            labels[current] = group.resIdLabel
            types[current] = TYPE_LABEL
            current++

            for (item in group.items) {
                types[current] = TYPE_ITEM
                items[current] = item
                current++
            }
        }
    }

    override fun getItemCount(): Int = sidebar.groups.map { it.items.size + 1 }.sum()


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            TYPE_LABEL -> (holder as VH_LABEL).bind(labels[position]!!)
            else -> (holder as VH).bind(items[position]!!, callback)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_LABEL -> VH_LABEL(LayoutInflater.from(parent?.context).inflate(R.layout.sidebar_label, parent, false))
        else -> VH(LayoutInflater.from(parent?.context).inflate(R.layout.sidebar_item, parent, false))
    }

    override fun getItemViewType(position: Int): Int = types[position]!!

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(sidebarItem: SidebarItem, callback: Callback) {
            itemView.textViewSidebar.setText(sidebarItem.resIdLabel)
            itemView.textViewSidebar.setOnClickListener {
                callback.onSidebarItemClicked(sidebarItem)
            }
        }
    }

    class VH_LABEL(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(resId: Int) {
            itemView.textViewSidebarLabel.setText(resId)
        }
    }

    companion object {
        val TYPE_LABEL = 0
        val TYPE_ITEM = 1
    }

    interface Callback {
        fun onSidebarItemClicked(item: SidebarItem)
    }
}