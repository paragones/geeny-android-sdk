package io.geeny.sdk.clients.custom.slots.source

class DiscoSource(resourceId: String, interval: Long, name: String, val fromValue: Int, val toValue: Int) : DistributionSource(name, resourceId, interval) {
    var value: Int = fromValue
    override fun nextRandomValue(): Int {
        value = if (value == fromValue) {
            toValue
        } else {
            fromValue
        }

        return value
    }
}