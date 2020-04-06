package com.example.traveljournal.journeys

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.database.TravelDatabaseDao
import com.example.traveljournal.formatJourneys
import kotlinx.coroutines.*

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var newJourney = MutableLiveData<Journey?>()

    private val journeys = database.getAllJourneys()

    val journeysStrings = Transformations.map(journeys) { journeys ->
        formatJourneys(journeys, application.resources)
    }

    val clearButtonVisible = Transformations.map(journeys) {
        it?.isNotEmpty()
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
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
            database.clearJourneys()
        }
    }
}