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
    fun deleteJourneys()

    @Delete
    fun deleteJourney(journey: Journey)

    @Query("SELECT * FROM journey_table ORDER BY journeyId DESC")
    fun getAllJourneys(): LiveData<List<Journey>>

    @Query("SELECT * FROM journey_table ORDER BY journeyId DESC LIMIT 1")
    fun getLatestJourney(): Journey?

    @Query("SELECT * from journey_table WHERE journeyId = :key")
    fun getJourneyWithId(key: Long): LiveData<Journey>

    @Transaction
    @Query("SELECT * FROM journey_table")
    fun getJourneysAndExperiences(): List<JourneyWithExperiences>

    // Methods for using the Experience class with Room
    @Insert
    fun insertExperience(experience: Experience)

    @Update
    fun updateExperience(experience: Experience)

    @Query("SELECT * from experience_table WHERE experienceId = :key")
    fun getExperience(key: Long): Experience

    @Query("SELECT * FROM experience_table ORDER BY experienceId DESC")
    fun getAllExperiences(): LiveData<List<Experience>>

    @Query("DELETE FROM experience_table")
    fun deleteExperiences()

    @Query("DELETE FROM experience_table WHERE journey_host_id = :key")
    fun deleteAllExperiencesFromJourney(key: Long)

    @Delete
    fun deleteExperience(experience: Experience)

    @Query("SELECT * FROM experience_table WHERE journey_host_id = :key ORDER BY experienceId DESC")
    fun getAllExperiencesFromJourney(key: Long): LiveData<List<Experience>>

    @Query("SELECT * from experience_table WHERE experienceId = :key")
    fun getExperienceWithId(key: Long): LiveData<Experience>

    // Methods for using the Memory class with Room
    @Insert
    fun insertMemory(memory: Memory)

    @Update
    fun updateMemory(memory: Memory)

    @Query("SELECT * from memory_table WHERE memoryId = :key")
    fun getMemory(key: Long): Memory

    @Query("SELECT * FROM memory_table ORDER BY memoryId DESC")
    fun getAllMemories(): LiveData<List<Memory>>

    @Query("DELETE FROM memory_table")
    fun deleteMemories()

    @Query("DELETE FROM memory_table WHERE experience_host_id = :key")
    fun deleteAllMemoriesFromExperience(key: Long)

    @Delete
    fun deleteMemory(memory: Memory)

    @Query("SELECT * FROM memory_table WHERE experience_host_id = :key ORDER BY memoryId DESC")
    fun getAllMemoriesFromExperience(key: Long): LiveData<List<Memory>>

    @Query("SELECT * FROM memory_table WHERE experience_host_id = :key ORDER BY memoryId DESC")
    fun getListAllMemoriesFromExperience(key: Long): List<Memory>

    @Query("SELECT * from memory_table WHERE memoryId = :key")
    fun getMemoryWithId(key: Long): LiveData<Memory>

    // Methods for using the MemoryPhoto class with Room
    @Insert
    fun insertMemoryPhoto(memoryPhoto: MemoryPhoto)

    @Query("SELECT * FROM memory_photo_table WHERE photoId = :key")
    fun getMemoryPhoto(key: Long): MemoryPhoto

    @Update
    fun updateMemoryPhoto(memoryPhoto: MemoryPhoto)

    @Query("SELECT * FROM memory_photo_table ORDER BY photoId DESC")
    fun getAllMemoryPhotos(): LiveData<List<MemoryPhoto>>

    @Query("DELETE FROM memory_photo_table")
    fun deletePhotos()

    @Query("DELETE FROM memory_photo_table WHERE photoId = :key")
    fun deleteMemoryPhotoWithId(key: Long)

    @Query("DELETE FROM memory_photo_table WHERE memory_host_id = :key")
    fun deleteAllPhotosFromMemory(key: Long)

    @Query("SELECT * FROM memory_photo_table WHERE memory_host_id = :key ORDER BY photoId DESC")
    fun getAllPhotosFromMemory(key: Long): LiveData<List<MemoryPhoto>>

    @Query("SELECT * FROM memory_photo_table WHERE memory_host_id = :key ORDER BY photoId DESC")
    fun getListAllPhotosFromMemory(key: Long): List<MemoryPhoto>

    // Methods for using the Notification class with Room
    @Insert
    fun insertNotification(notification: Notification)

    @Update
    fun updateNotification(notification: Notification)

    @Query("SELECT * FROM notification_table ORDER BY notificationId DESC")
    fun getAllNotifications(): LiveData<List<Notification>>

    @Query("SELECT * FROM notification_table WHERE notification_name = :key ORDER BY notificationId DESC LIMIT 1")
    fun getBackupNotification(key: String): Notification?
}