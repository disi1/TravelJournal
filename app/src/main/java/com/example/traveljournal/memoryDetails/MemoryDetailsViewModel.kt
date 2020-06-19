package com.example.traveljournal.memoryDetails

import android.util.Log
import androidx.lifecycle.*
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File


class MemoryDetailsViewModel(
    private val memoryKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val memory = MediatorLiveData<Memory>()

    val photoSrcUri = MutableLiveData<String>()

    val memoryPhotos = database.getAllPhotosFromMemory(memoryKey)

    val memoryDescription = MutableLiveData<String>()

    val memoryPhotoCaption = MutableLiveData<String>()

    val coverPhotoSrcUri = MutableLiveData<String>()

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private var _initiateImageImportFromCamera = MutableLiveData<Boolean?>()
    val initiateImageImportFromCamera: LiveData<Boolean?>
        get() = _initiateImageImportFromCamera

    private var _initiateCoverImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateCoverImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateCoverImageImportFromGallery

    private var _showSnackbarEventMemoryPhotosDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoryPhotosDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoryPhotosDeleted

    private var _showSnackbarEventMemoryDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoryDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoryDeleted

    private val _openDescriptionDialogFragment = MutableLiveData<Boolean?>()
    val openDescriptionDialogFragment: LiveData<Boolean?>
        get() = _openDescriptionDialogFragment

    private val _openPhotoDialogFragment = MutableLiveData<MemoryPhoto?>()
    val openPhotoDialogFragment: LiveData<MemoryPhoto?>
        get() = _openPhotoDialogFragment

    private val _openCoverPhotoDialogFragment = MutableLiveData<Boolean?>()
    val openCoverPhotoDialogFragment: LiveData<Boolean?>
        get() = _openCoverPhotoDialogFragment

    private val _memoryPhotoDeleted = MutableLiveData<Boolean?>()
    val memoryPhotoDeleted: LiveData<Boolean?>
        get() = _memoryPhotoDeleted

    private val _navigateToExperienceDetails = MutableLiveData<Long>()
    val navigateToExperienceDetails: LiveData<Long>
        get() = _navigateToExperienceDetails

    private val _openMemoryPhotoCaptionDialogFragment = MutableLiveData<Boolean?>()
    val openMemoryPhotoCaptionDialogFragment: LiveData<Boolean?>
        get() = _openMemoryPhotoCaptionDialogFragment

    fun doneDeletingMemoryPhoto() {
        _memoryPhotoDeleted.value = null
    }

    fun doneShowingSnackbarMemoryPhotosDeleted() {
        _showSnackbarEventMemoryPhotosDeleted.value = false
    }

    fun doneShowingSnackbarMemoryDeleted() {
        _showSnackbarEventMemoryDeleted.value = false
    }

    fun doneShowingDescriptionDialogFragment() {
        _openDescriptionDialogFragment.value = false
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneImportingImageFromCamera() {
        _initiateImageImportFromCamera.value = null
    }

    fun doneImportingCoverImageFromGallery() {
        _initiateCoverImageImportFromGallery.value = null
    }

    fun doneShowingMemoryPhotoCaptionDialogFragment() {
        _openMemoryPhotoCaptionDialogFragment.value = null
    }

    fun doneNavigatingToExperienceDetails() {
        _navigateToExperienceDetails.value = null
    }

    init {
        memory.addSource(database.getMemoryWithId(memoryKey), memory::setValue)
    }

    fun getMemory() = memory

    fun onChangeCoverPhotoClicked() {
        _initiateCoverImageImportFromGallery.value = true
    }

    fun onCoverPhotoChanged() {
        if(memory.value?.coverPhotoSrcUri != "") {
            val fileToDelete = File(memory.value?.coverPhotoSrcUri!!)
            if(fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }

    fun onUpdateMemoryDescription() {
        uiScope.launch {
            val oldMemory = memory.value ?: return@launch
            oldMemory.memoryDescription = memoryDescription.value.toString()
            updateMemory(oldMemory)
        }
    }

    fun onUpdateMemoryCoverPhoto() {
        uiScope.launch {
            val oldMemory = memory.value ?: return@launch
            oldMemory.coverPhotoSrcUri = coverPhotoSrcUri.value.toString()
            updateMemory(oldMemory)
        }
    }

    private suspend fun updateMemory(memory: Memory) {
        withContext(Dispatchers.IO) {
            database.updateMemory(memory)
        }
    }

    fun onCreateMemoryPhoto() {
        uiScope.launch {
            val memoryPhoto = MemoryPhoto(memoryHostId = memoryKey)
            memoryPhoto.photoSrcUri = photoSrcUri.value.toString()

            insertMemoryPhoto(memoryPhoto)
        }
    }

    private suspend fun insertMemoryPhoto(memoryPhoto: MemoryPhoto) {
        withContext(Dispatchers.IO) {
            database.insertMemoryPhoto(memoryPhoto)
        }
    }

    fun onUpdateMemoryPhotoCaption(memoryPhoto: MemoryPhoto) {
        uiScope.launch {
            val oldMemoryPhoto = memoryPhoto ?: return@launch
            oldMemoryPhoto.photoCaption = memoryPhotoCaption.value.toString()
            updateMemoryPhoto(oldMemoryPhoto)
        }
    }

    private suspend fun updateMemoryPhoto(memoryPhoto: MemoryPhoto) {
        withContext(Dispatchers.IO) {
            database.updateMemoryPhoto(memoryPhoto)
        }
    }

    fun onAddCaptionHereClicked() {
        _openMemoryPhotoCaptionDialogFragment.value = true
    }

    fun onMemoryPhotoClicked(memoryPhoto: MemoryPhoto) {
        _openPhotoDialogFragment.value = memoryPhoto
    }

    fun onAddPhotoFromGalleryButtonClicked() {
        _initiateImageImportFromGallery.value = true
    }

    fun onAddPhotoFromCameraButtonClicked() {
        _initiateImageImportFromCamera.value = true
    }

    fun onDescriptionTextClicked() {
        _openDescriptionDialogFragment.value = true
    }

    fun onMemoryCoverClicked() {
        _openCoverPhotoDialogFragment.value = true
    }

    fun onCloseMemoryCoverDialog() {
        _openCoverPhotoDialogFragment.value = false
    }

    fun onCloseMemoryPhotoDialog() {
        _openPhotoDialogFragment.value = null
    }

    fun onDeleteMemoryPhotos() {
        uiScope.launch {
            deleteMemoryPhotos(memoryKey)

            _showSnackbarEventMemoryPhotosDeleted.value = true
        }
    }

    private suspend fun deleteMemoryPhotos(memoryKey: Long) {
        withContext(Dispatchers.IO) {
            deletePhotosFromBackup()
            database.deleteAllPhotosFromMemory(memoryKey)
        }
    }

    fun onDeleteMemoryPhoto(memoryPhoto: MemoryPhoto) {
        uiScope.launch {
            deleteMemoryPhoto(memoryPhoto)
            _memoryPhotoDeleted.value = true
        }
    }

    private suspend fun deleteMemoryPhoto(memoryPhoto: MemoryPhoto) {
        withContext(Dispatchers.IO) {
            deletePhotoFromBackup(memoryPhoto)
            database.deleteMemoryPhotoWithId(memoryPhoto.photoId)
        }
    }

    private fun deletePhotosFromBackup() {
        memoryPhotos.value?.forEach {
            val fileToDelete = File(it.photoSrcUri)
            if(fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }

    private fun deletePhotoFromBackup(memoryPhoto: MemoryPhoto) {
        val fileToDelete = File(memoryPhoto.photoSrcUri)
        if(fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }

    fun onDeleteMemory() {
        uiScope.launch {
            memory.value?.memoryId?.let { deleteMemoryPhotos(it) }
            deleteMemory()

            _showSnackbarEventMemoryDeleted.value = true
            _navigateToExperienceDetails.value = memory.value?.experienceHostId
        }
    }

    private suspend fun deleteMemory() {
        withContext(Dispatchers.IO) {
            memory.value?.let { database.deleteMemory(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}