package com.example.messagereader

import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [SmsItem::class], version = 1, exportSchema = false)
abstract class SmsDatabase: RoomDatabase() {
    abstract fun smsDao(): SmsDao

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: SmsDatabase? = null
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(1)

        // @Synchronized
        fun getInstance(): SmsDatabase {
            return instance ?: synchronized(this) {
                val _instance = Room.databaseBuilder(
                        App.getContext(),
                        SmsDatabase::class.java,
                        "sms_database"
                    )
                    .addCallback(sRoomDatabaseCallback)
                    .allowMainThreadQueries()
                    .build()
                instance = _instance
                // return _instance
                _instance
            }
        }
        private val sRoomDatabaseCallback: Callback = object : Callback() {
            override fun onCreate(@NonNull db: SupportSQLiteDatabase) {
                super.onCreate(db)
                databaseWriteExecutor.execute {

                    // Populate the database in the background.
                    // If you want to start with more words, just add them.
                    val dao: SmsDao = instance!!.smsDao()

                }
            }
        }
    }
}