package com.leondroid.demo_app

import android.content.Context
import android.support.multidex.MultiDexApplication
import android.util.Log

class DemoApp : MultiDexApplication() {
    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        component = ApplicationComponent(this)

        component.sdk.init()
                .subscribe(
                        { Log.d(TAG, it.toString()) },
                        { Log.e(TAG, it.message, it) }
                )
    }

    override fun onTerminate() {
        component.sdk.tearDown()
                .subscribe(
                        { Log.d(TAG, it.toString()) },
                        { Log.e(TAG, it.message, it) }
                )
        super.onTerminate()
    }

    companion object {
        private val TAG = DemoApp::class.java.simpleName

        fun from(context: Context): DemoApp {
            return (context.applicationContext as DemoApp)
        }
    }
}