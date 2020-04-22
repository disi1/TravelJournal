package com.example.traveljournal.journeys

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var newJourney = MutableLiveData<Journey?>()

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

    private val _navigateToNewJourney = MutableLiveData<Journey>()

    val navigateToNewJourney: LiveData<Journey>
        get() = _navigateToNewJourney

    fun doneNavigating() {
        _navigateToNewJourney.value = null
    }

    init {
        initializeJourney()
    }

    private fun initializeJourney() {
        uiScope.launch {
            newJourney.value = getLatestJourneyFromDatabase()
        }
    }

    private suspend fun getLatestJourneyFromDatabase(): Journey? {
        return withContext(Dispatchers.IO) {
            var journey = database.getLatestJourney()
            if(journey?.placeName != "") {
                journey = null
            }
            journey
        }
    }

    /**
     * Executes when the NEW JOURNEY button is clicked
     */
    fun onNewJourney() {
        uiScope.launch {
            val journey = Journey()
            insert(journey)
            newJourney.value = getLatestJourneyFromDatabase()

            val currentJourney = newJourney.value ?: return@launch
            _navigateToNewJourney.value = currentJourney
        }
    }

    private suspend fun insert(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.insertJourney(journey)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            newJourney.value = null

            _showSnackbarEvent.value = true
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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

}