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
    @Query("SELECT * FROM sms")
    fun getAll(): LiveData<List<SmsItem>>

    @Query("SELECT * FROM sms WHERE send_status=:status")
    fun loadFromSendStatus(status: Int): LiveData<List<SmsItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg smsList: SmsItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sms: SmsItem)

    @Update
    fun update(sms: SmsItem)

    @Delete
    fun remove(sms: SmsItem)
}