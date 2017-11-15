package io.geeny.sample.ui.main

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sample.ui.main.clients.ble.characterstics.CharacteristicsFragment
import io.geeny.sample.ui.main.clients.ble.detail.BleClientFragment
import io.geeny.sample.ui.main.clients.ble.list.BleClientListFragment
import io.geeny.sample.ui.main.clients.custom.detail.CustomClientDetailFragment
import io.geeny.sample.ui.main.clients.custom.list.CustomClientListFragment
import io.geeny.sample.ui.main.clients.custom.slot.CustomResourceFragment
import io.geeny.sample.ui.main.clients.mqtt.detail.MqttDetailFragment
import io.geeny.sample.ui.main.routing.broker.BrokerListFragment
import io.geeny.sample.ui.main.clients.mqtt.list.MqttClientListFragment
import io.geeny.sample.ui.main.geeny.blethinglist.BleThingListFragment
import io.geeny.sample.ui.main.geeny.profile.ProfileFragment
import io.geeny.sample.ui.main.host.chart.ChartFragment
import io.geeny.sample.ui.main.routing.router.detail.RouterDetailFragment
import io.geeny.sample.ui.main.routing.router.list.RouterListFragment
import io.geeny.sample.ui.main.sidebar.SidebarFragment
import io.geeny.sample.ui.main.sidebar.SidebarItem
import io.geeny.sdk.clients.mqtt.MqttConfig
import io.geeny.sdk.common.DefaultKeyValueStore
import io.geeny.sdk.common.GLog
import io.geeny.sdk.routing.router.types.Route
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity :
        AppCompatActivity(),
        CustomClientListFragment.Container,
        MqttClientListFragment.Container,
        CustomClientDetailFragment.Container,
        BleClientListFragment.Container,
        BleClientFragment.Container,
        RouterListFragment.Container,

        SidebarFragment.Container {

    var toggle: ActionBarDrawerToggle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureNavigationDrawer()
        configureToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.mainContainer, BleClientListFragment.newInstance(), BleClientListFragment.TAG)
                    .add(R.id.sidebarContainer, SidebarFragment(), SidebarFragment.TAG)
                    .commit()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle?.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateDrawerStateNoAnimate(supportFragmentManager.backStackEntryCount > 0)
    }

    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setHomeButtonEnabled(true)
    }

    private fun configureNavigationDrawer() {
        toggle = object : ActionBarDrawerToggle(this, navigationDrawer,
                R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(view: View) {}
        }

        toggle?.isDrawerIndicatorEnabled = true
        navigationDrawer.setDrawerListener(toggle)
    }

    fun closeDrawer() {
        navigationDrawer!!.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.getItemId()

        when (itemId) {
            android.R.id.home -> {
                if (toggle!!.isDrawerIndicatorEnabled) {
                    navigationDrawer!!.openDrawer(GravityCompat.START)
                } else {
                    onBackPressed()
                }

                return true
            }
        }

        return true
    }

    private fun updateDrawerStateNoAnimate(isSubFragment: Boolean) {
        if (isSubFragment) {
            toggle?.isDrawerIndicatorEnabled = false
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toggle?.isDrawerIndicatorEnabled = true
        }
    }

    override fun openMqttClient(config: MqttConfig) {
        openSubFragment(MqttDetailFragment.newInstance(config.id()), MqttDetailFragment.TAG)
    }

    override fun openBleClient(address: String) {
        openSubFragment(BleClientFragment.newInstance(address), BleClientFragment.TAG)
    }

    override fun onCharacteristicClicked(address: String, characteristicUUID: String) {
        openSubFragment(CharacteristicsFragment.newInstance(address, characteristicUUID), CharacteristicsFragment.TAG)
    }

    override fun openRoute(route: Route) {
        openSubFragment(RouterDetailFragment.newInstance(route.identifier()), RouterDetailFragment.TAG)
    }

    override fun openCustomClient(address: String) {
        openSubFragment(CustomClientDetailFragment.newInstance(address), CustomClientDetailFragment.TAG)
    }

    override fun openCustomResource(clientId: String, resourceId: String) {
        openSubFragment(CustomResourceFragment.newInstance(clientId, resourceId), CustomResourceFragment.TAG)
    }

    override fun onSidebarItemClicked(item: SidebarItem) {

        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        updateDrawerStateNoAnimate(false)

        when (item) {
            SidebarItem.GEENY_PROFILE -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is ProfileFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, BleThingListFragment.newInstance(), BleThingListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.BLE_LIST -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is BleClientListFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, BleClientListFragment.newInstance(), BleClientListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.MQTT -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is MqttClientListFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, MqttClientListFragment.newInstance(), MqttClientListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.CUSTOM_CLIENTS -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is CustomClientListFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, CustomClientListFragment.newInstance(), CustomClientListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.BROKER -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is BrokerListFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, BrokerListFragment.newInstance(), BrokerListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.ROUTING -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is RouterListFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, RouterListFragment.newInstance(), RouterListFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.CHART -> {
                if (supportFragmentManager.findFragmentById(R.id.mainContainer) !is ChartFragment) {
                    transactionWithAnimation()
                            .replace(R.id.mainContainer, ChartFragment.newInstance(), ChartFragment.TAG)
                            .commit()
                }
            }
            SidebarItem.DUMP -> GLog.d("DATABASE_DUMP", (GatewayApp.from(this).component.sdk.keyValueStore as DefaultKeyValueStore).dump())
        }

        closeDrawer()
    }

    private fun openSubFragment(fragment: Fragment, tag: String) {
        transactionWithAnimation()
                .replace(R.id.mainContainer, fragment, tag)
                .addToBackStack(tag)
                .commit()
        updateDrawerStateNoAnimate(true)
    }

    private fun transactionWithAnimation(): FragmentTransaction = addSlideInTransition(supportFragmentManager.beginTransaction())
    private fun addDefaultTransition(transaction: FragmentTransaction): FragmentTransaction = transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out)
    private fun addSlideInTransition(transaction: FragmentTransaction): FragmentTransaction = transaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom, R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)
}