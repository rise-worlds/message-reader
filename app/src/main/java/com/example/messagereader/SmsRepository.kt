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

    @WorkerThread
    fun insert(word: SmsItem) {
        SmsDatabase.databaseWriteExecutor.execute { smsDao.insert(word) }
    }
}