package io.geeny.sample

import android.app.Application
import android.content.Context
import android.util.Log
import io.geeny.sdk.GeenySdk

class GatewayApp : Application() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        component = ApplicationComponent(this)
        component.sdk.init()
                .subscribe(
                    {Log.d(TAG, it.toString())},
                    {Log.e(TAG, it.message, it)}
                )
    }

    override fun onTerminate() {
        component.sdk.tearDown()
                .subscribe()
        super.onTerminate()
    }

    companion object {
        private val TAG = GatewayApp::class.java.simpleName
        fun from(context: Context): GatewayApp {
            return (context.applicationContext as GatewayApp)
        }
    }
}