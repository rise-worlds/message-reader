package com.example.messagereader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.ServiceState
import android.util.Log
import androidx.annotation.RequiresApi
import org.greenrobot.eventbus.EventBus


class App : Application() {
    private val TAG = "App"

    companion object {
        var _context:Application? = null
        @Synchronized
        fun getContext(): Context {
            return _context!!
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        _context = this

        EventBus.builder()
            // have a look at the index class to see which methods are picked up
            // if not in the index @Subscribe methods will be looked up at runtime (expensive)
            .addIndex(MyEventBusIndex())
            .installDefaultEventBus()

        val it = Intent(this, SmsRelayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "Starting the service in >=26 Mode")
            startForegroundService(it)
        } else {
            Log.i(TAG, "Starting the service in < 26 Mode")
            startService(it)
        }
    }

}