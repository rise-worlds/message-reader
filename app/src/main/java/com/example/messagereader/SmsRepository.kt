package com.example.messagereader

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData


class SmsRepository(private val smsDao: SmsDao) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: SmsRepository? = null

        @Synchronized
        fun getInstance(): SmsRepository {
            if (instance == null) {
                instance = SmsRepository(SmsDatabase.getInstance().smsDao())
            }
            return instance as SmsRepository
        }
    }

    fun getAll(): LiveData<List<SmsItem>> {
        return smsDao.getAll()
    }

    fun getFromSendStatus(status: Int): List<SmsItem> {
        return smsDao.getFromSendStatus(status)
    }

    @WorkerThread
    fun get(id: Int): SmsItem? {
        return smsDao.get(id)
    }

    @WorkerThread
    fun insert(item: SmsItem) {
        SmsDatabase.databaseWriteExecutor.execute {
            smsDao.insert(item)
        }
    }

    @WorkerThread
    fun updateSendStatus(id: Int, status: Int) {
        SmsDatabase.databaseWriteExecutor.execute { smsDao.setSendStatus(id, status) }
    }
}