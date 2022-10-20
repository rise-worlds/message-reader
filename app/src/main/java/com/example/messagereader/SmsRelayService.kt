package com.example.messagereader

import android.R
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.Binder
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.Date


class SmsRelayService : Service() {
    private val TAG = "SmsRelayService"

    private var mBinder = SmsBinder()
    private var thread: Thread? = null

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()

        EventBus.getDefault().register(this)
        thread = Thread {
            while (true) {
                val list = SmsRepository.getInstance().getFromSendStatus(0)
                list.forEach {

                    SmsRepository.getInstance().updateSendStatus(it.id, 1)
                }

                Thread.sleep(1000)
            }
        }
        thread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @Subscribe(sticky = true)
    fun handleEvent(event: SmsReceiver.NewSmsEvent) {
        getSmsFromPhone()

        // val className = this.javaClass.simpleName
        // val message = "#handleEvent: called for " + event.javaClass.simpleName
        // Toast.makeText(this, className + message, Toast.LENGTH_SHORT).show()
        // Log.d(className, message)

        // prevent event from re-delivering, like when leaving and coming back to app
        EventBus.getDefault().removeStickyEvent(event)
    }


    inner class SmsBinder : Binder() {
        @Throws(InterruptedException::class)
        fun readSms() {
            getSmsFromPhone()
        }
    }

    @SuppressLint("Range", "Recycle", "SimpleDateFormat")
    private fun getSmsFromPhone() {
        val cr = contentResolver
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.READ,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
        )
        val cur: Cursor? = cr.query(Telephony.Sms.CONTENT_URI, projection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
        Log.i(TAG, "---------getSmsFromPhone  111")

        if (null == cur) {
            Log.e(TAG, "读取短信出错")
            return
        }
        while (cur.moveToNext()) {
            val id = cur.getInt(cur.getColumnIndex(Telephony.Sms._ID))
            val number = cur.getString(cur.getColumnIndex(Telephony.Sms.ADDRESS)) // 手机号
            val read = cur.getInt(cur.getColumnIndex(Telephony.Sms.READ)) == 1
            val body = cur.getString(cur.getColumnIndex(Telephony.Sms.BODY))
            val timestamp = cur.getLong(cur.getColumnIndex(Telephony.Sms.DATE))
            val type = cur.getShort(cur.getColumnIndex(Telephony.Sms.TYPE))

            val date = Date(timestamp) // 时间
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val receiveTime: String = format.format(date)

            Log.i(TAG, "---------getSmsFromPhone  ${id}, ${number}, read: $read, ${body}, ${receiveTime}, $type")

            val saved = SmsRepository.getInstance().get(id)
            if (saved == null) {
                val item = SmsItem(id, number, body, timestamp, 0)
                SmsRepository.getInstance().insert(item)
            }
        }
        cur.close()
    }
}
