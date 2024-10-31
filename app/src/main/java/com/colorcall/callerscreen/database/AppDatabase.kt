package com.colorcall.callerscreen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Background::class,Contact::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backgroundDao(): BackgroundDao
    abstract fun contactDao(): ContactDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "colorcall"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
