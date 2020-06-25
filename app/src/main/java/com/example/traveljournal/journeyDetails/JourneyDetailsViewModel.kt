package com.example.traveljournal.journeyDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class JourneyDetailsViewModel(
    private val journeyKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val journey = MediatorLiveData<Journey>()

    val coverPhotoSrcUri = MutableLiveData<String>()

    val experiences = database.getAllExperiencesFromJourney(journeyKey)

    private var _showSnackbarEventExperiencesDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventExperiencesDeleted: LiveData<Boolean>
        get() = _showSnackbarEventExperiencesDeleted

    private var _showSnackbarEventJourneyDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventJourneyDeleted: LiveData<Boolean>
        get() = _showSnackbarEventJourneyDeleted

    fun getJourney() = journey

    init {
        journey.addSource(database.getJourneyWithId(journeyKey), journey::setValue)
    }

    private val _navigateToJourneys = MutableLiveData<Boolean?>()
    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    private val _navigateToNewExperience = MutableLiveData<Long>()
    val navigateToNewExperience: LiveData<Long>
        get() = _navigateToNewExperience

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private val _openCoverPhotoDialogFragment = MutableLiveData<Boolean?>()
    val openCoverPhotoDialogFragment: LiveData<Boolean?>
        get() = _openCoverPhotoDialogFragment

    fun doneNavigatingToJourneys() {
        _navigateToJourneys.value = null
    }

    fun doneNavigatingToNewExperience() {
        _navigateToNewExperience.value = null
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneShowingSnackbarExperiencesDeleted() {
        _showSnackbarEventExperiencesDeleted.value = false
    }

    fun doneShowingSnackbarJourneyDeleted() {
        _showSnackbarEventJourneyDeleted.value = false
    }

    fun onNewExperience() {
        _navigateToNewExperience.value = journeyKey
    }

    fun onNavigateToJourneysHome() {
        _navigateToJourneys.value = true
    }

    fun onChangeCoverPhotoClicked() {
        _initiateImageImportFromGallery.value = true
    }

    fun onCoverPhotoChanged() {
        if(journey.value?.coverPhotoSrcUri != "") {
            val fileToDelete = File(journey.value?.coverPhotoSrcUri!!)
            if(fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }

    fun onUpdateJourney() {
        uiScope.launch {
            val oldJourney = journey.value ?: return@launch
            oldJourney.coverPhotoAttributions = ""
            oldJourney.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            updateJourney(oldJourney)
        }
    }

    private suspend fun updateJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.updateJourney(journey)
        }
    }

    fun onCloseJourneyCoverDialog() {
        _openCoverPhotoDialogFragment.value = false
    }

    fun onJourneyCoverClicked() {
        _openCoverPhotoDialogFragment.value = true
    }

    fun onDeleteJourney() {
        uiScope.launch {
            deleteJourneyCoverPhoto()

            journey.value?.journeyId?.let { deleteExperiences(it) }

            deleteJourney()

            _showSnackbarEventJourneyDeleted.value = true
            _navigateToJourneys.value = true
        }
    }

    fun onDeleteExperiences() {
        uiScope.launch {
            deleteExperiences(journeyKey)

            _showSnackbarEventExperiencesDeleted.value = true
        }
    }

    private suspend fun deleteExperiences(journeyKey: Long) {
        withContext(Dispatchers.IO) {
            experiences.value?.forEach { experience ->
                deleteExperienceCoverPhoto(experience)

                deleteMemoriesUnderExperience(experience)
            }
            database.deleteAllExperiencesFromJourney(journeyKey)
        }
    }

    private suspend fun deleteMemoriesUnderExperience(experience: Experience) {
        withContext(Dispatchers.IO) {
            database.getListAllMemoriesFromExperience(experience.experienceId).forEach { memory ->
                deleteMemoryCoverPhoto(memory)

                deleteMemoryPhotosUnderMemory(memory)
            }
            database.deleteAllMemoriesFromExperience(experience.experienceId)
        }
    }

    private suspend fun deleteMemoryPhotosUnderMemory(memory: Memory) {
        withContext(Dispatchers.IO) {
            database.getListAllPhotosFromMemory(memory.memoryId).forEach { memoryPhoto ->
                val memoryPhotoDelete = File(memoryPhoto.photoSrcUri)
                if (memoryPhotoDelete.exists()) {
                    memoryPhotoDelete.delete()
                }
            }
            database.deleteAllPhotosFromMemory(memory.memoryId)
        }
    }

    private suspend fun deleteJourney() {
        withContext(Dispatchers.IO) {
            journey.value?.let { database.deleteJourney(it) }
        }
    }

    private fun deleteJourneyCoverPhoto() {
        val journeyCoverPhotoDelete = File(journey.value?.coverPhotoSrcUri!!)
        if(journeyCoverPhotoDelete.exists()) {
            journeyCoverPhotoDelete.delete()
        }
    }

    private fun deleteExperienceCoverPhoto(experience: Experience) {
        val experienceCoverPhotoDelete = File(experience.coverPhotoSrcUri)
        if(experienceCoverPhotoDelete.exists()) {
            experienceCoverPhotoDelete.delete()
        }
    }

    private fun deleteMemoryCoverPhoto(memory: Memory) {
        val memoryCoverPhotoDelete = File(memory.coverPhotoSrcUri)
        if (memoryCoverPhotoDelete.exists()) {
            memoryCoverPhotoDelete.delete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}