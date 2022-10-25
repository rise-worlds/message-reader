package com.example.messagereader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.messagereader.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val TAG = "MainActivity"
    private val RC_READ_SMS_PERM = 124
    private val RC_ALL_SD_FILES_ACCESS = 125

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + App.getContext().packageName)
                startActivityForResult(intent, RC_ALL_SD_FILES_ACCESS)
            }
        }

        val sp = this.applicationContext.getSharedPreferences("config", MODE_PRIVATE)
        val editor = sp.edit()
        val deviceSerial = intent.getStringExtra("DeviceSerial")
        if (deviceSerial != null) {
            this.deviceSerial = deviceSerial
        } else {
            this.deviceSerial = sp.getString("DeviceSerial", "")!!
        }

        val phoneNumber = getNativePhoneNumber()
        if (phoneNumber.isNotEmpty()) {
            editor.putString("DevicePhoneNumber", phoneNumber)
            editor.apply();

            val dir = File(Environment.getExternalStorageDirectory().absolutePath + "/Download/")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "myPhoneNumber.txt")
            if (file.exists()) {
                file.delete()
            }
            file.writeText("{\"DevicePhoneNumber\":\"${phoneNumber}\"}")
        }

        if (this.deviceSerial.isNotEmpty()) {
            editor.putString("DeviceSerial", this.deviceSerial)
            editor.apply();

            navController.navigate(R.id.SecondFragment)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // 授予权限
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // 请求权限被拒
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
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

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getNativePhoneNumber(): String {
        val perms = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,

            )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val nativePhoneNumber = telephonyManager.line1Number
            Log.d(TAG, "getNativePhoneNumber: $nativePhoneNumber")
            return nativePhoneNumber
        }

        return ""
    }

}