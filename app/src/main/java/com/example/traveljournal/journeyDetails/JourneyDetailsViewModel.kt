package com.example.traveljournal.journeyDetails

import android.os.Environment
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import androidx.core.content.contentValuesOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class JourneyDetailsViewModel(
    private val journeyKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val journey = MediatorLiveData<Journey>()

    val experiences = database.getAllExperiencesFromJourney(journeyKey)

    private var _showSnackbarEventExperiencesDeleted = MutableLiveData<Boolean>()

    val showSnackbarEventExperiencesDeleted: LiveData<Boolean>
        get() = _showSnackbarEventExperiencesDeleted

    fun doneShowingSnackbarExperiencesDeleted() {
        _showSnackbarEventExperiencesDeleted.value = false
    }

    fun getJourney() = journey

    init {
        journey.addSource(database.getJourneyWithId(journeyKey), journey::setValue)
    }

    private val _navigateToNewExperience = MutableLiveData<Long>()
    val navigateToNewExperience: LiveData<Long>
        get() = _navigateToNewExperience

    private val _navigateToExperienceDetails = MutableLiveData<Long>()
    val navigateToExperienceDetails
        get() = _navigateToExperienceDetails

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

            _showSnackbarEventExperiencesDeleted.value = true
        }
    }

    suspend fun clearExperiences(journeyKey: Long) {
        withContext(Dispatchers.IO) {
            database.clearAllExperiencesFromJourney(journeyKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}