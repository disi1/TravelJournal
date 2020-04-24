package com.example.traveljournal.journeyDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class JourneyDetailsViewModel(
    private val journeyKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val journey = MediatorLiveData<Journey>()

    val experiences = database.getAllExperiencesFromJourney(journeyKey)

    private var _showSnackbarEventExpDeleted = MutableLiveData<Boolean>()

    val showSnackbarEventExpDeleted: LiveData<Boolean>
        get() = _showSnackbarEventExpDeleted

    fun doneShowingSnackbarExpDeleted() {
        _showSnackbarEventExpDeleted.value = false
    }

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

    private val _navigateToExperienceDetails = MutableLiveData<Long>()
    val navigateToExperienceDetails
        get() = _navigateToExperienceDetails

    fun doneNavigating() {
        _navigateToJourneys.value = null
    }

    fun doneNavigatingToNewExperience() {
        _navigateToNewExperience.value = null
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

    fun onClear() {
        uiScope.launch {
            clearExperiences(journeyKey)

            _showSnackbarEventExpDeleted.value = true
        }
    }

    suspend fun clearExperiences(journeyKey: Long) {
        withContext(Dispatchers.IO) {
            database.clearAllExperiencesFromJourney(journeyKey)
        }
    }

    fun onClose() {
        _navigateToJourneys.value = true
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}