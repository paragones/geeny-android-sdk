package io.geeny.sdk.clients.custom.slots.source

import android.os.Handler
import android.os.HandlerThread
import io.geeny.sdk.common.TypeConverters
import java.util.*

abstract class DistributionSource(name: String, resourceID: String, val interval: Long = 1000) : Source(name, resourceID) {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    protected val random = Random()

    override fun onEnabled() {
        thread = HandlerThread(name)
        thread!!.start()
        handler = Handler(thread!!.looper)
        next(nextRandomValue())
    }

    private fun next(value: Int) {
        if (isEnabled) {
            notify(TypeConverters.intToBytes(value))
            handler!!.postDelayed({ next(nextRandomValue()) }, interval)
        }
    }

    override fun onDisabled() {
        thread?.quit()
    }

    abstract fun nextRandomValue(): Int
}