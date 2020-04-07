package com.example.traveljournal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Journey::class], version = 4, exportSchema = false)
abstract class TravelDatabase : RoomDatabase() {

    abstract val travelDatabaseDao : TravelDatabaseDao
    companion object{
        @Volatile
        private var INSTANCE: TravelDatabase? = null

        fun getInstance(context: Context) : TravelDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null) {
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