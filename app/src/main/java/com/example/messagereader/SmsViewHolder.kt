package com.example.messagereader

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messagereader.SmsRelayService.Companion.smsStatus
import java.text.SimpleDateFormat
import java.util.Date

class SmsViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    private var id : Int = 0
    private val phoneView: TextView
    private val timeView: TextView
    private val reportView: TextView
    private val bodyView: TextView

    init {
        phoneView = itemView.findViewById(R.id.text_phone)
        timeView = itemView.findViewById(R.id.text_time)
        reportView = itemView.findViewById(R.id.text_report)
        bodyView = itemView.findViewById(R.id.text_body)
    }

    @SuppressLint("SimpleDateFormat")
    fun bind(item: SmsItem) {
        this.id = item.id
        this.phoneView.text = item.phoneNum
        val date = Date(item.receiveTime) // 时间
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val receiveTime: String = format.format(date)
        this.timeView.text = receiveTime
        this.reportView.text = smsStatus[item.sendStatus]
        this.bodyView.text = item.body
    }

    companion object {
        fun create(parent: ViewGroup): SmsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
            return SmsViewHolder(view)
        }
    }
}