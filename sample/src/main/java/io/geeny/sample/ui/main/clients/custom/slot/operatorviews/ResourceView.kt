package io.geeny.sample.ui.main.clients.custom.slot.operatorviews

import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot

interface ResourceView {
    fun bind(client: Client, slot: Slot)
}