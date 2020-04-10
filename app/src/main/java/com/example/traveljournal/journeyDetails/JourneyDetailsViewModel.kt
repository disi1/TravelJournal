package com.example.traveljournal.journeyDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.Job

class JourneyDetailsViewModel(
    private val journeyKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val journey = MediatorLiveData<Journey>()

    fun getJourney() = journey

    init {
        journey.addSource(database.getJourneyWithId(journeyKey), journey::setValue)
    }

    private val _navigateToJourneys = MutableLiveData<Boolean?>()

    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun doneNavigating() {
        _navigateToJourneys.value = null
    }

    fun onClose() {
        _navigateToJourneys.value = true
    }
}