package com.example.messagereader

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log


class ClipperReceiver : BroadcastReceiver() {
    private val TAG = "ClipboardReceiver"

    var ACTION_GET = "clipper.get"
    var ACTION_GET_SHORT = "get"
    var ACTION_SET = "clipper.set"
    var ACTION_SET_SHORT = "set"
    var EXTRA_TEXT = "text"

    private fun isActionGet(action: String?): Boolean {
        return ACTION_GET == action || ACTION_GET_SHORT == action
    }

    private fun isActionSet(action: String?): Boolean {
        return ACTION_SET == action || ACTION_SET_SHORT == action
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cb: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (isActionSet(intent.action)) {
            Log.d(TAG, "Setting text into clipboard")
            val text = intent.getStringExtra(EXTRA_TEXT)
            if (text != null) {
                cb.text = text
                resultCode = Activity.RESULT_OK
                resultData = "Text is copied into clipboard."
            } else {
                resultCode = Activity.RESULT_CANCELED
                resultData = "No text is provided. Use -e text \"text to be pasted\""
            }
        } else if (isActionGet(intent.action)) {
            Log.d(TAG, "Getting text from clipboard")
            val clip: CharSequence = cb.text
            if (clip != null) {
                Log.d(TAG, String.format("Clipboard text: %s", clip))
                resultCode = Activity.RESULT_OK
                resultData = clip.toString()
            } else {
                resultCode = Activity.RESULT_CANCELED
                resultData = ""
            }
        }
    }
}