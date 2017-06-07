package io.geeny.sample.ui.common.log

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sdk.clients.ble.GattResult
import java.util.*


class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    val data: MutableList<GattResult> = ArrayList()

    fun add(result: GattResult) {
        data.add(0, result)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): LogViewHolder = LogViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.row_log, parent, false) as LogRow)
    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(holder: LogViewHolder?, position: Int) {
        holder?.row?.bind(data[position])
    }


    class LogViewHolder(val row: LogRow) : RecyclerView.ViewHolder(row)
}