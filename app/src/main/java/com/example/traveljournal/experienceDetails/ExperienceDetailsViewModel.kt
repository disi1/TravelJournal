package com.example.traveljournal.experienceDetails

import androidx.lifecycle.*
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

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

    val experienceDescription = MutableLiveData<String>()

    private var _showSnackbarEventExpUpdated = MutableLiveData<Boolean>()

    val showSnackbarEventExperienceUpdated: LiveData<Boolean>
        get() = _showSnackbarEventExpUpdated

    fun doneShowingSnackbarExperienceUpdated() {
        _showSnackbarEventExpUpdated.value = false
    }

    fun onUpdateExperience() {
        uiScope.launch {
            val oldExperience = experience.value ?: return@launch
            oldExperience.experienceDescription = experienceDescription.value.toString()
            updateExperience(oldExperience)

            _showSnackbarEventExpUpdated.value = true
        }
    }

    private suspend fun updateExperience(experience: Experience) {
        withContext(Dispatchers.IO) {
            database.updateExperience(experience)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}