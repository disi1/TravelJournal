package com.example.traveljournal.experience

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class NewExperienceViewModel (
    private val journeyKey: Long = 0L,
    val database: TravelDatabaseDao
): ViewModel() {

    val selectedExperiencePlaceName = MutableLiveData<String>()

    val selectedExperiencePlaceAddress = MutableLiveData<String>()

    val experienceName = MutableLiveData<String>()

    val coverPhotoSrcUri = MutableLiveData<String>()

    val coverPhotoAttributions = MutableLiveData<String>()

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToJourneyDetails = MutableLiveData<Long>()
    val navigateToJourneyDetails: LiveData<Long>
        get() = _navigateToJourneyDetails

    fun doneNavigatingToJourneyDetails() {
        _navigateToJourneyDetails.value = null
    }

    private val _bitmapCoverLoaded = MutableLiveData<Boolean?>()
    val bitmapCoverLoaded: LiveData<Boolean?>
        get() = _bitmapCoverLoaded

    fun onBitmapCoverLoaded() {
        _bitmapCoverLoaded.value = true
    }

    fun onCreateExperience() {
        uiScope.launch {
            val experience = Experience(journeyHostId = journeyKey)
            experience.coverPhotoAttributions = coverPhotoAttributions.value.toString()
            experience.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            experience.experienceName = experienceName.value.toString()
            experience.experiencePlaceName = selectedExperiencePlaceName.value.toString()
            experience.experiencePlaceAddress = selectedExperiencePlaceAddress.value.toString()

            insertExperience(experience)

            _navigateToJourneyDetails.value = journeyKey
        }
    }

    private suspend fun insertExperience(experience: Experience) {
        withContext(Dispatchers.IO) {
            database.insertExperience(experience)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}