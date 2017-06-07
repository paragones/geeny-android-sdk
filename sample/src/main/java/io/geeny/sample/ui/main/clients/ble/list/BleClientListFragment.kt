package io.geeny.sample.ui.main.clients.ble.list

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sample.ui.main.clients.ble.list.view.BleClientListRow
import io.geeny.sdk.clients.ble.BleClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ble_client_list.*

class BleClientListFragment : BaseFragment(), BleClientListView {
    lateinit var presenter: BleConnectionListPresenter
    lateinit var adapter: BleConnectionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = GatewayApp.from(activity)

        presenter = BleConnectionListPresenter(app.component.sdk, Schedulers.single(), AndroidSchedulers.mainThread())
        adapter = BleConnectionListAdapter()
    }

    override fun layout(): Int = R.layout.fragment_ble_client_list

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewScannedDevices.setHasFixedSize(true)

        // use a linear layout manager
        recyclerViewScannedDevices.layoutManager = LinearLayoutManager(activity)
        recyclerViewScannedDevices.adapter = adapter

        buttonScan.setOnClickListener {
            if (buttonScan.text == "Scan") {
                buttonScan.text = "Stop Scan"
                presenter.scanBleDevices()
                showProgress(true)
            } else {
                showProgress(false)
                buttonScan.text = "Scan"
                presenter.stopScanning()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar!!.title = "BLE Devices"

        adapter.callback = object : BleConnectionListAdapter.Callback {
            override fun onDeviceSelected(address: String) {
                (activity as Container).openBleClient(address)
            }
        }

        presenter.attach(this)
        checkLocationPermission()

    }


    override fun onStop() {
        adapter.callback = null
        presenter.detach()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(activity)
                        .setTitle("Check")
                        .setMessage("We need that permission")
                        .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(activity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    }
                }
                return
            }
        }
    }

    override fun onListUpdated(listOfClients: List<BleClient>) {
        adapter.data = listOfClients
    }

    override fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    companion object {
        val TAG = BleClientListFragment::class.java.simpleName
        val MY_PERMISSIONS_REQUEST_LOCATION = 8371

        fun newInstance(): BleClientListFragment = BleClientListFragment()
    }

    interface Container {
        fun openBleClient(address: String)
    }
}


class BleConnectionListAdapter : RecyclerView.Adapter<BleConnectionListAdapter.ViewHolder>() {

    var callback: Callback? = null

    override fun onBindViewHolder(viewHolder: BleConnectionListAdapter.ViewHolder?, position: Int) {
        viewHolder!!.bind(data!![position])
        viewHolder.itemView.setOnClickListener {
            if (callback != null) {
                callback!!.onDeviceSelected(data!![position].address())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BleConnectionListAdapter.ViewHolder = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.row_ble, parent, false) as BleClientListRow)


    override fun getItemCount(): Int = data?.size ?: 0

    var data: List<BleClient>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(private val row: BleClientListRow) : RecyclerView.ViewHolder(row) {
        fun bind(connection: BleClient) {
            row.bindConnection(connection)
        }
    }

    interface Callback {
        fun onDeviceSelected(address: String)
    }
}