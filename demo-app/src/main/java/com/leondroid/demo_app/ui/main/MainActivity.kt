package com.leondroid.demo_app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.leondroid.demo_app.R
import com.leondroid.demo_app.ui.launch.LaunchActivity
import com.leondroid.demo_app.ui.main.blething.ThingFragment
import com.leondroid.demo_app.ui.main.blethings.BleThingsFragment
import com.leondroid.demo_app.ui.main.virtualthing.VirtualThingFragment
import com.leondroid.demo_app.ui.main.virtualthings.VirtualThingsFragment
import io.geeny.sample.ui.common.presenter.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(),
        BleThingsFragment.Container,
        VirtualThingsFragment.Container,
        MainView {

    private lateinit var presenter: MainPresenter

    private var isScanning = false
    private var bleSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MainPresenter(sdk(), app().ioScheduler, app().mainScheduler)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.containerMain, BleThingsFragment(), BleThingsFragment.TAG)
                    .commit()

            buttonBleThings.isSelected = true
            bleSelected = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.signout -> {
                presenter.signout()
                true
            }
            R.id.scan -> {
                if (isScanning) {
                    presenter.stopScan()
                } else {
                    presenter.startScan()
                }
                isScanning = !isScanning
                if (isScanning) {
                    item.setTitle(R.string.stop_scan)
                } else {
                    item.setTitle(R.string.scan)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
        checkLocationPermission()


        buttonBleThings.setOnClickListener {
            if (!bleSelected) {
                bleSelected = true
                transactionWithAnimation()
                        .replace(R.id.containerMain, BleThingsFragment(), BleThingsFragment.TAG)
                        .commit()
                buttonBleThings.isSelected = true
                buttonVirtualThings.isSelected = false
            }
        }

        buttonVirtualThings.setOnClickListener {
            if (bleSelected) {
                bleSelected = false
                transactionWithAnimation()
                        .replace(R.id.containerMain, VirtualThingsFragment(), VirtualThingsFragment.TAG)
                        .commit()
                buttonBleThings.isSelected = false
                buttonVirtualThings.isSelected = true
            }
        }
    }

    override fun onStop() {
        buttonBleThings.setOnClickListener {}
        buttonVirtualThings.setOnClickListener {}
        presenter.detach()
        super.onStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateDrawerStateNoAnimate(supportFragmentManager.backStackEntryCount > 0)
    }


    private fun updateDrawerStateNoAnimate(isSubFragment: Boolean) {
        if (isSubFragment) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            containerSelectors.visibility = ViewGroup.GONE
        } else {
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            containerSelectors.visibility = ViewGroup.VISIBLE
        }
    }

    override fun onVirtualThingClicked(address: String) {
        openSubFragment(VirtualThingFragment.newInstance(address), VirtualThingFragment.TAG)
    }

    override fun onThingClicked(address: String) {
        openSubFragment(ThingFragment.newInstance(address), ThingFragment.TAG)
    }

    private fun openSubFragment(fragment: Fragment, tag: String) {
        transactionWithAnimation()
                .replace(R.id.containerMain, fragment, tag)
                .addToBackStack(tag)
                .commit()
        updateDrawerStateNoAnimate(true)
    }

    private fun transactionWithAnimation(): FragmentTransaction = addSlideInTransition(supportFragmentManager.beginTransaction())
    private fun addDefaultTransition(transaction: FragmentTransaction): FragmentTransaction = transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out)
    private fun addSlideInTransition(transaction: FragmentTransaction): FragmentTransaction = transaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom, R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)

    override fun restartApp() {
        val intent = Intent(this, LaunchActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
    }


    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Check")
                        .setMessage("We need that permission")
                        .setPositiveButton("Ok", { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
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
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    }
                }
                return
            }
        }
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 8371
    }
}
