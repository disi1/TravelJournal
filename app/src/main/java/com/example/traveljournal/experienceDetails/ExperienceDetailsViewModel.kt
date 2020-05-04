package com.example.traveljournal.experienceDetails

import android.util.Log
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

    val memories = database.getAllMemoriesFromExperience(experienceKey)

    init {
        experience.addSource(database.getExperienceWithId(experienceKey), experience::setValue)
    }

    fun getExperience() = experience

    val experienceDescription = MutableLiveData<String>()

    val addDescriptionLabelVisible = Transformations.map(experience) {
        "" == it.experienceDescription
    }

    val descriptionLabelVisible = Transformations.map(experience) {
        "" != it.experienceDescription
    }

    private var _showSnackbarEventExpUpdated = MutableLiveData<Boolean>()
    val showSnackbarEventExperienceUpdated: LiveData<Boolean>
        get() = _showSnackbarEventExpUpdated

    private var _showSnackbarEventMemoriesDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoriesDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoriesDeleted

    private val _navigateToNewMemory = MutableLiveData<Long>()
    val navigateToNewMemory: LiveData<Long>
        get() = _navigateToNewMemory

    private val _navigateToMemoryDetails = MutableLiveData<Long>()
    val navigateToMemoryDetails
        get() = _navigateToMemoryDetails

    private val _openDialogFragment = MutableLiveData<Boolean?>()
    val openDialogFragment: LiveData<Boolean?>
        get() = _openDialogFragment

    fun doneShowingSnackbarExperienceUpdated() {
        _showSnackbarEventExpUpdated.value = false
    }

    fun doneShowingSnackbarMemoriesDeleted() {
        _showSnackbarEventMemoriesDeleted.value = false
    }

    fun doneNavigatingToNewMemory() {
        _navigateToNewMemory.value = null
    }

    fun doneNavigatingToMemoryDetails() {
        _navigateToMemoryDetails.value = null
    }

    fun onNewMemory() {
        _navigateToNewMemory.value = experienceKey
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

    fun onMemoryClicked(memoryId: Long) {
        _navigateToMemoryDetails.value = memoryId
    }

    fun onClear() {
        uiScope.launch {
            clearMemories(experienceKey)

            _showSnackbarEventMemoriesDeleted.value = true
        }
    }

    fun onDescriptionTextClicked() {
        _openDialogFragment.value = true
    }

    suspend fun clearMemories(experienceKey: Long) {
        withContext(Dispatchers.IO) {
            database.clearAllMemoriesFromExperience(experienceKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}