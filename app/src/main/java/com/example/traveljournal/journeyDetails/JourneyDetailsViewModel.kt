package com.example.traveljournal.journeyDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Journey
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

    fun onDeleteExperiences() {
        uiScope.launch {
            deleteExperiences(journeyKey)

            _showSnackbarEventExperiencesDeleted.value = true
        }
    }

    private suspend fun deleteExperiences(journeyKey: Long) {
        withContext(Dispatchers.IO) {
            val attachedExperiences = database.getAllExperiencesFromJourney(journeyKey)
            attachedExperiences.value?.forEach { experience ->
                val attachedMemories = database.getAllMemoriesFromExperience(experience.experienceId)
                attachedMemories.value?.forEach { memory ->
                    database.deleteAllPhotosFromMemory(memory.memoryId)
                }
                database.deleteAllMemoriesFromExperience(experience.experienceId)
            }
            database.deleteAllExperiencesFromJourney(journeyKey)
        }
    }

    fun onDeleteJourney() {
        uiScope.launch {
            journey.value?.journeyId?.let { deleteExperiences(it) }
            deleteJourney()

            _showSnackbarEventJourneyDeleted.value = true
            _navigateToJourneys.value = true
        }
    }

    private suspend fun deleteJourney() {
        withContext(Dispatchers.IO) {
            journey.value?.let { database.deleteJourney(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}