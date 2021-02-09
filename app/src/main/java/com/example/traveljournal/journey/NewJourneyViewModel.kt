package com.example.traveljournal.journey

import android.util.Log
import androidx.lifecycle.*
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class NewJourneyViewModel(val database: TravelDatabaseDao) : ViewModel() {

    val selectedPlaceName = MutableLiveData<String>()

    val selectedPlaceAddress = MutableLiveData<String>()

    val coverPhotoSrcUri = MutableLiveData<String>()

    val coverPhotoAttributions = MutableLiveData<String>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToJourneys = MutableLiveData<Boolean?>()
    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    private val _bitmapCoverLoaded = MutableLiveData<Boolean?>()
    val bitmapCoverLoaded: LiveData<Boolean?>
        get() = _bitmapCoverLoaded

    fun onBitmapCoverLoaded(state: Boolean) {
        _bitmapCoverLoaded.value = state
    }

    fun doneNavigating() {
        _navigateToJourneys.value = null
    }

    fun onCreateJourney() {
        uiScope.launch {
            val journey = Journey()

            if (coverPhotoAttributions.value == null) {
                journey.coverPhotoAttributions = ""
            } else {
                journey.coverPhotoAttributions = coverPhotoAttributions.value.toString()
            }

            if (coverPhotoSrcUri.value == null) {
                journey.coverPhotoSrcUri = ""
            } else {
                journey.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            }

            if (selectedPlaceName.value == null) {
                journey.placeName = ""
            } else {
                journey.placeName = selectedPlaceName.value.toString()
            }

            if (selectedPlaceAddress.value == null) {
                journey.placeAddress = ""
            } else {
                journey.placeAddress = selectedPlaceAddress.value.toString()
            }

            insertJourney(journey)

            _navigateToJourneys.value = true
        }
    }

    private suspend fun insertJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.insertJourney(journey)
        }
    }

    fun onCancelJourney() {
        if (coverPhotoSrcUri.value != null) {
            val fileToDelete = File(coverPhotoSrcUri.value!!)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
        _navigateToJourneys.value = true
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}