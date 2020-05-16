package com.example.traveljournal.memoryDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    val coverPhotoSrcUri = MutableLiveData<String>()

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private var _initiateCoverImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateCoverImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateCoverImageImportFromGallery

    private var _showSnackbarEventMemoryPhotosDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoryPhotosDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoryPhotosDeleted

    private val _openDescriptionDialogFragment = MutableLiveData<Boolean?>()
    val openDescriptionDialogFragment: LiveData<Boolean?>
        get() = _openDescriptionDialogFragment

    private val _openPhotoDialogFragment = MutableLiveData<MemoryPhoto?>()
    val openPhotoDialogFragment: LiveData<MemoryPhoto?>
        get() = _openPhotoDialogFragment

    private val _openCoverPhotoDialogFragment = MutableLiveData<Boolean?>()
    val openCoverPhotoDialogFragment: LiveData<Boolean?>
        get() = _openCoverPhotoDialogFragment

    fun doneShowingSnackbarMemoryPhotosDeleted() {
        _showSnackbarEventMemoryPhotosDeleted.value = false
    }

    fun doneShowingDescriptionDialogFragment() {
        _openDescriptionDialogFragment.value = false
    }

    fun doneImportingImageFromGallery() {
        _initiateImageImportFromGallery.value = null
    }

    fun doneImportingCoverImageFromGallery() {
        _initiateCoverImageImportFromGallery.value = null
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

    fun onMemoryPhotoClicked(memoryPhoto: MemoryPhoto) {
        _openPhotoDialogFragment.value = memoryPhoto
    }

    fun onNewMemoryPhotoClicked() {
        _initiateImageImportFromGallery.value = true
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

    fun onClear() {
        uiScope.launch {
            clearMemoryPhotos(memoryKey)

            _showSnackbarEventMemoryPhotosDeleted.value = true
        }
    }

    private suspend fun clearMemoryPhotos(memoryKey: Long) {
        withContext(Dispatchers.IO) {
            deletePhotosFromBackup()
            database.deleteAllPhotosFromMemory(memoryKey)
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}