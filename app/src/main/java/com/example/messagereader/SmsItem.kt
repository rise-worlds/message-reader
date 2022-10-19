package com.example.messagereader

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.NonNull;

@Entity(tableName = "sms")
data class SmsItem (
    @PrimaryKey
    @NonNull
    val id: Int,
    @ColumnInfo(name="phone_num") val phoneNum: String,
    @ColumnInfo(name="body") val body: String,
    @ColumnInfo(name="receive_time") val receiveTime: Long,
    @ColumnInfo(name="send_status") val sendStatus: Int,
)