package io.geeny.sdk.clients.custom.slots.source

class DiscoSource(resourceId: String, interval: Long): DistributionSource("Disco", resourceId, interval) {
    var value: Int = 0
    override fun nextRandomValue(): Int {
        value = if(value == 0) {
            1
        } else {
            0
        }

        return value
    }
}