package com.example.traveljournal.experienceDetails

import androidx.lifecycle.*
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class ExperienceDetailsViewModel(
    private val experienceKey: Long = 0L,
    dataSource: TravelDatabaseDao): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val experience = MediatorLiveData<Experience>()

    val coverPhotoSrcUri = MutableLiveData<String>()

    val memories = database.getAllMemoriesFromExperience(experienceKey)

    init {
        experience.addSource(database.getExperienceWithId(experienceKey), experience::setValue)
    }

    fun getExperience() = experience

    val experienceDescription = MutableLiveData<String>()

    private var _showSnackbarEventMemoriesDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoriesDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoriesDeleted

    private val _navigateToNewMemory = MutableLiveData<Long>()
    val navigateToNewMemory: LiveData<Long>
        get() = _navigateToNewMemory

    private val _navigateToMemoryDetails = MutableLiveData<Long>()
    val navigateToMemoryDetails: LiveData<Long>
        get() = _navigateToMemoryDetails

    private val _openDialogFragment = MutableLiveData<Boolean?>()
    val openDialogFragment: LiveData<Boolean?>
        get() = _openDialogFragment

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private val _openCoverPhotoDialogFragment = MutableLiveData<Boolean?>()
    val openCoverPhotoDialogFragment: LiveData<Boolean?>
        get() = _openCoverPhotoDialogFragment

    fun doneShowingSnackbarMemoriesDeleted() {
        _showSnackbarEventMemoriesDeleted.value = false
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneNavigatingToNewMemory() {
        _navigateToNewMemory.value = null
    }

    fun doneNavigatingToMemoryDetails() {
        _navigateToMemoryDetails.value = null
    }

    fun doneShowingDialogFragment() {
        _openDialogFragment.value = false
    }

    fun onNewMemory() {
        _navigateToNewMemory.value = experienceKey
    }

    fun onChangeCoverPhotoClicked() {
        _initiateImageImportFromGallery.value = true
    }

    fun onCoverPhotoChanged() {
        if(experience.value?.coverPhotoSrcUri != "") {
            val fileToDelete = File(experience.value?.coverPhotoSrcUri!!)
            if(fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }

    fun onUpdateExperienceDescription() {
        uiScope.launch {
            val oldExperience = experience.value ?: return@launch
            oldExperience.experienceDescription = experienceDescription.value.toString()
            updateExperience(oldExperience)
        }
    }

    fun onUpdateExperienceCoverPhoto() {
        uiScope.launch {
            val oldExperience = experience.value ?: return@launch
            oldExperience.coverPhotoAttributions = ""
            oldExperience.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            updateExperience(oldExperience)
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

    fun onExperienceCoverClicked() {
        _openCoverPhotoDialogFragment.value = true
    }

    fun onCloseExperienceCoverDialog() {
        _openCoverPhotoDialogFragment.value = false
    }

    suspend fun clearMemories(experienceKey: Long) {
        withContext(Dispatchers.IO) {
            database.deleteAllMemoriesFromExperience(experienceKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}