package com.example.messagereader

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.greenrobot.eventbus.EventBus


class App : Application() {

    companion object {
        var _context:Application? = null
        @Synchronized
        fun getContext(): Context {
            return _context!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        _context = this

        startService(Intent(this, SmsRelayService::class.java))

        EventBus.builder()
            // have a look at the index class to see which methods are picked up
            // if not in the index @Subscribe methods will be looked up at runtime (expensive)
            .addIndex(MyEventBusIndex())
            .installDefaultEventBus()
    }

}