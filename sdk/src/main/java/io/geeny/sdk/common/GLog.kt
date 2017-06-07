package io.geeny.sdk.common

import android.util.Log
import java.lang.Exception

class GLog {

    companion object {
        val geenyTag = "GEENY_LOG."

        var level: LogLevel = LogLevel.ALL


        fun i(tag: String, msg: String) {
            if (level == LogLevel.NONE) {
                return
            }
            Log.i(geenyTag + tag, Thread.currentThread().name + " - " + msg)
        }

        fun d(tag: String, msg: String) {
            if (level == LogLevel.NONE) {
                return
            }
            Log.d(geenyTag + tag, Thread.currentThread().name + " - " + msg)
        }

        fun e(tag: String, msg: String?, throwable: Throwable) {
            if (level == LogLevel.NONE) {
                return
            }
            Log.e(geenyTag + tag, msg, throwable)
        }
    }


}

enum class LogLevel {
    NONE,
    ALL
}