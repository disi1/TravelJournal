package com.example.traveljournal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Journey::class,
        Experience::class,
        Memory::class,
        MemoryPhoto::class,
        Notification::class
    ], version = 19, exportSchema = true
)
abstract class TravelDatabase : RoomDatabase() {

    abstract val travelDatabaseDao: TravelDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: TravelDatabase? = null

        fun getInstance(context: Context): TravelDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TravelDatabase::class.java,
                        "travel_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}