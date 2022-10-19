package com.example.messagereader

import android.app.Application
import android.content.Context

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
    }

}