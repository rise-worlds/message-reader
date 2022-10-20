package com.example.messagereader

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms ORDER BY id DESC")
    fun getAll(): LiveData<List<SmsItem>>

    @Query("SELECT * FROM sms WHERE id=:id")
    fun get(id: Int): SmsItem?

    @Query("SELECT * FROM sms WHERE send_status=:status")
    fun getFromSendStatus(status: Int): List<SmsItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg smsList: SmsItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(sms: SmsItem)

    @Update
    fun update(sms: SmsItem)

    @Query("UPDATE sms SET send_status=:status WHERE id=:id")
    fun setSendStatus(id: Int, status: Int)

    @Delete
    fun remove(sms: SmsItem)
}