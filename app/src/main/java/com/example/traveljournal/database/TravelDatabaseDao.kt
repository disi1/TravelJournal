package com.example.traveljournal.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TravelDatabaseDao {
    // Methods for using the Journey class with Room
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

    @Transaction
    @Query("SELECT * FROM journey_table ORDER BY journeyId DESC")
    fun getJourneyWithExperiences(): LiveData<List<JourneyWithExperiences>>

    // Methods for using the Experience class with Room
    @Insert
    fun insertExperience(experience: Experience)

    @Update
    fun updateExperience(experience: Experience)

    @Query("SELECT * from experience_table WHERE experienceId = :key")
    fun getExperience(key: Long): Experience

    @Query("DELETE FROM experience_table")
    fun clearExperiences()

    @Query("DELETE FROM experience_table WHERE journey_host_id = :key")
    fun clearAllExperiencesFromJourney(key: Long)

    @Delete
    fun deleteExperience(experience: Experience)

    @Query("SELECT * FROM experience_table ORDER BY experienceId DESC")
    fun getAllExperiences(): LiveData<List<Experience>>

    @Query("SELECT * FROM experience_table WHERE journey_host_id = :key ORDER BY experienceId DESC")
    fun getAllExperiencesFromJourney(key: Long): LiveData<List<Experience>>

    @Query("SELECT * FROM experience_table ORDER BY experienceId DESC LIMIT 1")
    fun getLatestExperience(): Experience?

    @Query("SELECT * from experience_table WHERE experienceId = :key")
    fun getExperienceWithId(key: Long): LiveData<Experience>
}