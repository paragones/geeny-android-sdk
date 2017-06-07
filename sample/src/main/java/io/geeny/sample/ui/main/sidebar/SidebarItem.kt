package io.geeny.sample.ui.main.sidebar

import io.geeny.sample.R

enum class SidebarItem(val resIdLabel: Int) {
    GEENY_PROFILE(R.string.label_geeny_profile),
    BLE_LIST(R.string.label_ble_list),
    MQTT(R.string.label_mqtt),
    CUSTOM_CLIENTS(R.string.label_custom_clients),
    BROKER(R.string.label_broker),
    ROUTING(R.string.label_routing),
    CHART(R.string.label_chart),
    DUMP(R.string.label_dump)
}

data class SidebarGroup(val resIdLabel: Int, val items: List<SidebarItem>)

data class Sidebar(val groups: List<SidebarGroup>)