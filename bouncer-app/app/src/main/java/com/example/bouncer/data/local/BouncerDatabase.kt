package com.example.bouncer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bouncer.data.BanRecord

@Database(entities = [BanRecord::class], version = 1, exportSchema = false)
abstract class BouncerDatabase : RoomDatabase() {
    abstract fun banRecordDao(): BanRecordDao

    companion object {
        @Volatile
        private var INSTANCE: BouncerDatabase? = null

        fun getInstance(context: Context): BouncerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BouncerDatabase::class.java,
                    "bouncer_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
