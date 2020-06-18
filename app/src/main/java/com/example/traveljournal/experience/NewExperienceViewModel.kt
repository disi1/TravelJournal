package com.example.traveljournal.experience

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class NewExperienceViewModel (
    private val journeyKey: Long = 0L,
    val database: TravelDatabaseDao
): ViewModel() {

    val selectedExperiencePlaceName = MutableLiveData<String>()

    val selectedExperiencePlaceAddress = MutableLiveData<String>()

    val experienceName = MutableLiveData<String>()

    val experienceDescription = MutableLiveData<String>()

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

            if(experienceName.value == null) {
                experience.experienceName = ""
            } else {
                experience.experienceName = experienceName.value.toString()
            }

            if(experienceDescription.value == null) {
                experience.experienceDescription = ""
            } else {
                experience.experienceDescription = experienceDescription.value.toString()
            }

            if(selectedExperiencePlaceName.value == null) {
                experience.experiencePlaceName = ""
            } else {
                experience.experiencePlaceName = selectedExperiencePlaceName.value.toString()
            }

            if(selectedExperiencePlaceAddress.value == null) {
                experience.experiencePlaceAddress = ""
            } else {
                experience.experiencePlaceAddress = selectedExperiencePlaceAddress.value.toString()
            }

            if(coverPhotoAttributions.value == null) {
                experience.coverPhotoAttributions = ""
            } else {
                experience.coverPhotoAttributions = coverPhotoAttributions.value.toString()
            }

            if(coverPhotoSrcUri.value == null) {
                experience.coverPhotoSrcUri = ""
            } else {
                experience.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            }

            insertExperience(experience)

            _navigateToJourneyDetails.value = journeyKey
        }
    }

    private suspend fun insertExperience(experience: Experience) {
        withContext(Dispatchers.IO) {
            database.insertExperience(experience)
        }
    }

    fun onCancelExperience() {
        if(coverPhotoSrcUri.value != null) {
            val fileToDelete = File(coverPhotoSrcUri.value!!)
            if(fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
        _navigateToJourneyDetails.value = journeyKey
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}