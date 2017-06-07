package io.geeny.sdk.clients.custom.slots.channel

import io.geeny.sdk.clients.custom.slots.ResourceType

class BypassChannel(resourceId: String, val name: String) : MapChannel(resourceId) {
    override fun name(): String = name

    override fun type(): ResourceType = ResourceType.CHANNEL

    override fun map(array: ByteArray): ByteArray = array
}