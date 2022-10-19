package com.example.messagereader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony.Sms
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.blankj.utilcode.util.ShellUtils
import com.example.messagereader.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    private val TAG = "MainActivity"
    private val RC_READ_SMS_PERM = 124

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var deviceSerial: String = ""

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        requestPermissions()

        val sp = this.applicationContext.getSharedPreferences("config", MODE_PRIVATE)
        val deviceSerial = intent.getStringExtra("DeviceSerial")
        if (deviceSerial != null) {
            this.deviceSerial = deviceSerial
        } else {
            this.deviceSerial = sp.getString("DeviceSerial", "")!!
        }
        if (this.deviceSerial.isNotEmpty()) {
            val editor = sp.edit()
            editor.putString("DeviceSerial", this.deviceSerial)
            editor.apply()

            navController.navigate(R.id.action_SaveDeviceIDFragment_to_SmsListFragment)
        } else {
            navController.navigate(R.id.action_SmsListFragment_to_SaveDeviceIDFragment)
        }

        getSmsFromPhone()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // 授予权限
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
        getSmsFromPhone()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // 请求权限被拒
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.i(TAG, "---------onRationaleAccepted  $requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.i(TAG, "---------onRationaleDenied  $requestCode")
    }

    private fun requestPermissions() {
        val perms = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            // 没有权限，进行权限请求
            EasyPermissions.requestPermissions(
                this, "The App needs to read text messages.",
                RC_READ_SMS_PERM, *perms
            )
        }
    }

    @SuppressLint("Range", "Recycle", "SimpleDateFormat")
    fun getSmsFromPhone() {
        val cr = contentResolver
        val projection = arrayOf(
            Sms._ID,
            Sms.ADDRESS,
            Sms.READ,
            Sms.BODY,
            Sms.DATE,
            Sms.TYPE,
        )
        val cur: Cursor? = cr.query(Sms.CONTENT_URI, projection, null, null, Sms.DEFAULT_SORT_ORDER)
        Log.i(TAG, "---------getSmsFromPhone  111")

        if (null == cur) {
            Log.e(TAG, "读取短信出错")
            return
        }
        while (cur.moveToNext()) {
            val id = cur.getInt(cur.getColumnIndex(Sms._ID))
            val number = cur.getString(cur.getColumnIndex(Sms.ADDRESS)) // 手机号
            val read = cur.getInt(cur.getColumnIndex(Sms.READ)) == 1
            val body = cur.getString(cur.getColumnIndex(Sms.BODY))
            val timestamp = cur.getLong(cur.getColumnIndex(Sms.DATE))
            val type = cur.getShort(cur.getColumnIndex(Sms.TYPE))

            val date = Date(timestamp) // 时间
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val receiveTime: String = format.format(date)

            Log.i(TAG, "---------getSmsFromPhone  ${id}, ${number}, read: $read, ${body}, ${receiveTime}, $type")

            val item = SmsItem(id, number, body, timestamp, 0)
            SmsRepository.getInstance().insert(item)
        }
        cur.close()
    }
}