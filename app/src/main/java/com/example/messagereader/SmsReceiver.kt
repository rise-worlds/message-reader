package com.example.messagereader

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*


class SmsReceiver : BroadcastReceiver() {

    class NewSmsEvent
    class UpdateSmsListEvent

    @Suppress("DEPRECATION")
    @SuppressLint("SimpleDateFormat", "UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        var msg: SmsMessage?
        if (null != bundle) {
            val sp = App.getContext().getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
            // val deviceSerial = sp.getString("DeviceSerial", "")
            val phoneNumber = sp.getString("DevicePhoneNumber", "")
            if (phoneNumber.isNullOrEmpty()) {
                return
            }

            val smsObj = bundle["pdus"] as Array<*>?
            // Log.i(TAG, "---------MyReceiver")
            for (`object` in smsObj!!) {
                msg = SmsMessage.createFromPdu(`object` as ByteArray)
                val date = Date(msg.timestampMillis) // 时间
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val receiveTime: String = format.format(date)
                Log.i(TAG, "number:${msg.originatingAddress}   body:${msg.displayMessageBody}  time:${receiveTime}")
                // Toast.makeText(context, msg.displayMessageBody, Toast.LENGTH_SHORT).show();

                val item = SmsItem(0, msg.displayOriginatingAddress, msg.displayMessageBody, msg.timestampMillis, 0)
                SmsRelayService.report(phoneNumber, item)
            }
            EventBus.getDefault().post(NewSmsEvent())
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"

    }
}