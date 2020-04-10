package com.example.traveljournal.journey

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.*

class NewJourneyViewModel (
        private val journeyKey: Long = 0L,
        val database: TravelDatabaseDao) : ViewModel() {

    val selectedPlaceName = MutableLiveData<String>()

    val selectedPlaceAddress = MutableLiveData<String>()

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // @TODO this does not work. it needs a fix
    val createButtonVisible = Transformations.map(selectedPlaceName) {
        null != it
    }

    private val _navigateToJourneys = MutableLiveData<Boolean?>()

    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    fun doneNavigating() {
        _navigateToJourneys.value = null
    }

    fun onCreateJourney() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val journey = database.getJourney(journeyKey) ?: return@withContext
                journey.placeName = selectedPlaceName.value.toString()
                journey.placeAddress = selectedPlaceAddress.value.toString()
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