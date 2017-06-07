package io.geeny.sdk.clients.custom.slots.source

import android.os.Handler
import android.os.HandlerThread
import io.geeny.sdk.common.TypeConverters

class FunctionalSource(name: String, resourceId: String, private val func: (Int) -> Int, val delay: Long = 1000) : Source(name, resourceId) {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null

    override fun onEnabled() {
        thread = HandlerThread(name)
        thread!!.start()
        handler = Handler(thread!!.looper)
        next(0)
    }


    override fun onDisabled() {
        thread?.quit()
    }

    private fun next(value: Int) {
        if (isEnabled) {
            notify(TypeConverters.intToBytes(func(value)))
            handler?.postDelayed({ next(value + 1) }, delay)
        }
    }

}