package com.example.traveljournal.experienceDetails

import androidx.lifecycle.*
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File

class ExperienceDetailsViewModel(
    private val experienceKey: Long = 0L,
    dataSource: TravelDatabaseDao
) : ViewModel() {

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

    private var _showSnackbarEventExperienceDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventExperienceDeleted: LiveData<Boolean>
        get() = _showSnackbarEventExperienceDeleted

    private val _navigateToNewMemory = MutableLiveData<Long>()
    val navigateToNewMemory: LiveData<Long>
        get() = _navigateToNewMemory

    private val _navigateToJourneyDetails = MutableLiveData<Long>()
    val navigateToJourneyDetails: LiveData<Long>
        get() = _navigateToJourneyDetails

    private val _openDialogFragment = MutableLiveData<Boolean?>()
    val openDialogFragment: LiveData<Boolean?>
        get() = _openDialogFragment

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private val _openCoverPhotoDialogFragment = MutableLiveData<Boolean?>()
    val openCoverPhotoDialogFragment: LiveData<Boolean?>
        get() = _openCoverPhotoDialogFragment

    private val _navigateToJourneys = MutableLiveData<Boolean?>()
    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    fun doneShowingSnackbarMemoriesDeleted() {
        _showSnackbarEventMemoriesDeleted.value = false
    }

    fun doneShowingSnackbarExperienceDeleted() {
        _showSnackbarEventExperienceDeleted.value = false
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneNavigatingToNewMemory() {
        _navigateToNewMemory.value = null
    }

    fun doneNavigatingToJourneyDetails() {
        _navigateToJourneyDetails.value = null
    }

    fun doneShowingDialogFragment() {
        _openDialogFragment.value = false
    }

    fun doneNavigatingToJourneysHome() {
        _navigateToJourneys.value = null
    }

    fun onNavigateToJourneysHome() {
        _navigateToJourneys.value = true
    }

    fun onNewMemory() {
        _navigateToNewMemory.value = experienceKey
    }

    fun onChangeCoverPhotoClicked() {
        _initiateImageImportFromGallery.value = true
    }

    fun onCoverPhotoChanged() {
        if (experience.value?.coverPhotoSrcUri != "") {
            val fileToDelete = File(experience.value?.coverPhotoSrcUri!!)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }

    fun onUpdateExperienceDescription(newDescription: String) {
        uiScope.launch {
            val oldExperience = experience.value ?: return@launch
            oldExperience.experienceDescription = newDescription
            updateExperience(oldExperience)
            experienceDescription.value = newDescription
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

    fun onDescriptionTextClicked() {
        _openDialogFragment.value = true
    }

    fun onExperienceCoverClicked() {
        _openCoverPhotoDialogFragment.value = true
    }

    fun onCloseExperienceCoverDialog() {
        _openCoverPhotoDialogFragment.value = false
    }

    fun onDeleteMemories() {
        uiScope.launch {
            deleteMemories(experienceKey)

            _showSnackbarEventMemoriesDeleted.value = true
        }
    }

    fun onDeleteExperience() {
        uiScope.launch {
            deleteExperienceCoverPhoto()

            experience.value?.experienceId?.let { deleteMemories(it) }
            deleteExperience()

            _showSnackbarEventExperienceDeleted.value = true
            _navigateToJourneyDetails.value = experience.value?.journeyHostId
        }
    }

    private suspend fun deleteMemories(experienceKey: Long) {
        withContext(Dispatchers.IO) {
            memories.value?.forEach { memory ->
                deleteMemoryCoverPhoto(memory)

                deleteMemoryPhotosUnderMemory(memory)
            }
            database.deleteAllMemoriesFromExperience(experienceKey)
        }
    }

    private suspend fun deleteExperience() {
        withContext(Dispatchers.IO) {
            experience.value?.let { database.deleteExperience(it) }
        }
    }

    private fun deleteExperienceCoverPhoto() {
        val experienceCoverPhotoDelete = File(experience.value?.coverPhotoSrcUri!!)
        if (experienceCoverPhotoDelete.exists()) {
            experienceCoverPhotoDelete.delete()
        }
    }

    private fun deleteMemoryCoverPhoto(memory: Memory) {
        val memoryCoverPhotoDelete = File(memory.coverPhotoSrcUri)
        if (memoryCoverPhotoDelete.exists()) {
            memoryCoverPhotoDelete.delete()
        }
    }

    private suspend fun deleteMemoryPhotosUnderMemory(memory: Memory) {
        withContext(Dispatchers.IO) {
            database.getListAllPhotosFromMemory(memory.memoryId).forEach { memoryPhoto ->
                val memoryPhotoDelete = File(memoryPhoto.photoSrcUri)
                if (memoryPhotoDelete.exists()) {
                    memoryPhotoDelete.delete()
                }
            }
            database.deleteAllPhotosFromMemory(memory.memoryId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}