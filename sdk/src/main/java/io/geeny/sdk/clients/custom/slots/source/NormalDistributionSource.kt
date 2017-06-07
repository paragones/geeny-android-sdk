package io.geeny.sdk.clients.custom.slots.source

class NormalDistributionSource(name: String, resourceId: String, val mean: Int, val deviation: Int, interval: Long = 1000) : DistributionSource(name, resourceId, interval) {
    override fun nextRandomValue(): Int = (random.nextGaussian() * deviation + mean).toInt()
}