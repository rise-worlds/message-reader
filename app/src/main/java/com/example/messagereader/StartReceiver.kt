package com.example.messagereader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class StartReceiver : BroadcastReceiver()
{
    private val TAG = "StartReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Intent(context, SmsRelayService::class.java).also {
                // it.action = Action.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i(TAG, "Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                Log.i(TAG, "Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }

}