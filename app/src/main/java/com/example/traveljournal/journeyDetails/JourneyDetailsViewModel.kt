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

    fun doneShowingSnackbarExperiencesDeleted() {
        _showSnackbarEventExperiencesDeleted.value = false
    }

    fun getJourney() = journey

    init {
        journey.addSource(database.getJourneyWithId(journeyKey), journey::setValue)
    }

    private val _navigateToNewExperience = MutableLiveData<Long>()
    val navigateToNewExperience: LiveData<Long>
        get() = _navigateToNewExperience

    private val _navigateToExperienceDetails = MutableLiveData<Long>()
    val navigateToExperienceDetails
        get() = _navigateToExperienceDetails

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    fun doneNavigatingToNewExperience() {
        _navigateToNewExperience.value = null
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneNavigatingToExperienceDetails() {
        _navigateToExperienceDetails.value = null
    }

    fun onNewExperience() {
        _navigateToNewExperience.value = journeyKey
    }

    fun onExperienceClicked(experienceId: Long) {
        _navigateToExperienceDetails.value = experienceId
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
            oldJourney.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            updateJourney(oldJourney)
        }
    }

    private suspend fun updateJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.updateJourney(journey)
        }
    }

    fun onClear() {
        uiScope.launch {
            clearExperiences(journeyKey)

            _showSnackbarEventExperiencesDeleted.value = true
        }
    }

    private suspend fun clearExperiences(journeyKey: Long) {
        withContext(Dispatchers.IO) {
            database.deleteAllExperiencesFromJourney(journeyKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}