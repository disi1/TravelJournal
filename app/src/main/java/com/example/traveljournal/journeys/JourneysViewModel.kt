package com.example.traveljournal.journeys

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val journeys = database.getAllJourneys()

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    private var _showSnackBarEventJourneyDeleted = MutableLiveData<Boolean>()

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent
    val showSnackBarEventJourneyDeleted: LiveData<Boolean>
        get() = _showSnackBarEventJourneyDeleted

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }
    fun doneShowingSnackbarJourneyDeleted() {
        _showSnackBarEventJourneyDeleted.value = false
    }

    private val _navigateToNewJourney = MutableLiveData<Boolean?>()

    val navigateToNewJourney: LiveData<Boolean?>
        get() = _navigateToNewJourney

    fun doneNavigating() {
        _navigateToNewJourney.value = null
    }

    fun onNewJourney() {
        _navigateToNewJourney.value = true
    }

    fun onClear() {
        uiScope.launch {
            clear()
            _showSnackbarEvent.value = true
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clearMemories()
            database.clearExperiences()
            database.clearJourneys()
        }
    }

    fun onDeleteJourney(journey: Journey) {
        uiScope.launch {
            deleteJourney(journey)

            _showSnackBarEventJourneyDeleted.value = true
        }
    }

    private suspend fun deleteJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.deleteJourney(journey)
        }
    }

    private val _navigateToJourneyDetails = MutableLiveData<Long>()
    val navigateToJourneyDetails
        get() = _navigateToJourneyDetails

    fun onJourneyClicked(id: Long) {
        _navigateToJourneyDetails.value = id
    }

    fun onJourneyDetailsNavigated() {
        _navigateToJourneyDetails.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}