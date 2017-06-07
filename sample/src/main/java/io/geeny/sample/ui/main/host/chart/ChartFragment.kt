package io.geeny.sample.ui.main.host.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.custom.slots.Slot
import kotlinx.android.synthetic.main.fragment_chart.*


class ChartFragment : BaseFragment(), ChartView, AdapterView.OnItemSelectedListener {
    private lateinit var presenter: ChartPresenter
    private lateinit var adapter: SpinnerAdapter

    override fun layout(): Int = R.layout.fragment_chart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChartPresenter(sdk(), component().ioScheduler, component().mainScheduler)
        adapter = SpinnerAdapter()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerResource.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        presenter.loadResources()
        spinnerResource.onItemSelectedListener = this
    }

    override fun onStop() {
        presenter.detach()
        spinnerResource.onItemSelectedListener = null
        super.onStop()
    }


    override fun showResources(slots: List<Slot>) {
        adapter.data = slots.map { it.id() }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        presenter.pickResource(adapter.data!![position])
    }

    override fun showData(chartData: ChartData<Int>) {
        chartLayout.setData(chartData)
    }

    companion object {
        val TAG = ChartFragment::class.java.simpleName
        fun newInstance() = ChartFragment()
    }
}

class SpinnerAdapter : BaseAdapter() {

    var data: List<String>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = LayoutInflater.from(parent?.context)?.inflate(R.layout.view_topic, parent, false) as TextView
        textView.text = getItem(position)
        return textView
    }

    override fun getItem(position: Int): String = data!![position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = data?.size ?: 0

}