package com.example.traveljournal.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TravelDatabaseDao {
    @Insert
    fun insertJourney(journey: Journey)

    @Update
    fun updateJourney(journey: Journey)

    @Query("SELECT * from journey_table WHERE journeyId = :key")
    fun getJourney(key: Long): Journey

    @Query("DELETE FROM journey_table")
    fun clearJourneys()

    @Delete
    fun deleteJourney(journey: Journey)

    @Query("SELECT * FROM journey_table ORDER BY journeyId DESC")
    fun getAllJourneys(): LiveData<List<Journey>>

    @Query("SELECT * FROM journey_table ORDER BY journeyId DESC LIMIT 1")
    fun getLatestJourney(): Journey?

    @Query("SELECT * from journey_table WHERE journeyId = :key")
    fun getJourneyWithId(key: Long): LiveData<Journey>
}