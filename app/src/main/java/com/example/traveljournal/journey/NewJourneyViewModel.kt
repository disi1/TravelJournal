package com.example.traveljournal.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class NewJourneyViewModel(
        private val journeyKey: Long = 0L,
        val database: TravelDatabaseDao) : ViewModel() {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToJourneys = MutableLiveData<Boolean?>()

    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    fun doneNavigating() {
        _navigateToJourneys.value = null
    }

    fun onCreateJourney(placeId: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val journey = database.getJourney(journeyKey) ?: return@withContext
                journey.placeId = placeId
                database.updateJourney(journey)
            }
            _navigateToJourneys.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}