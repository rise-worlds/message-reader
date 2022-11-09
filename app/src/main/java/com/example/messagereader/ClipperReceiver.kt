package com.example.messagereader

import android.app.Activity
import android.content.*
import android.util.Log

// am broadcast -n com.example.messagereader/.ClipperReceiver -a clipper.set -e "text" "电脑上的内容"
// am broadcast -n com.example.messagereader/.ClipperReceiver -a clipper.get -esn "text"
class ClipperReceiver : BroadcastReceiver() {
    private val TAG = "ClipboardReceiver"

    private val ACTION_GET = "clipper.get"
    private val ACTION_SET = "clipper.set"
    private val EXTRA_TEXT = "text"

    override fun onReceive(context: Context, intent: Intent) {
        val cb: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (ACTION_SET == intent.action) {
            Log.d(TAG, "Setting text into clipboard")
            val text = intent.getStringExtra(EXTRA_TEXT)
            if (text != null) {
                val clipData = ClipData.newPlainText("text", text)
                cb.setPrimaryClip(clipData)
                resultCode = Activity.RESULT_OK
                resultData = "Text is copied into clipboard."
            } else {
                resultCode = Activity.RESULT_CANCELED
                resultData = "No text is provided. Use -e text \"text to be pasted\""
            }
        } else if (ACTION_GET == intent.action) {
            Log.d(TAG, "Getting text from clipboard")
            val clip: ClipData? = cb.primaryClip
            if (clip != null) {
                Log.d(TAG, String.format("Clipboard text: %s", clip))
                resultCode = Activity.RESULT_OK
                resultData = (clip.getItemAt(0).text as String?)!!
            } else {
                resultCode = Activity.RESULT_CANCELED
                resultData = ""
            }
        } else {
            Log.d(TAG, "unknown action")
        }
    }
}