package com.example.traveljournal.experienceDetails

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ExperienceDetailsViewModel(
    private val experienceKey: Long = 0L,
    dataSource: TravelDatabaseDao): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val experience = MediatorLiveData<Experience>()

    init {
        experience.addSource(database.getExperienceWithId(experienceKey), experience::setValue)
    }

    fun getExperience() = experience

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}