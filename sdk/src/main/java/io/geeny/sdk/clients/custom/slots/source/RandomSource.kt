package io.geeny.sdk.clients.custom.slots.source

import android.os.Handler
import android.os.HandlerThread
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.TypeConverters
import java.util.*

class RandomSource(name: String, resourceId: String) : Source(name, resourceId) {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null


    val random = Random()

    init {
    }

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
            handler!!.postDelayed({
                GLog.d("CUNT", "next")
                notify(TypeConverters.intToBytes(value))
                next(random.nextInt())
            }, 1000)
        }
    }
}