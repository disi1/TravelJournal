package com.example.traveljournal.memoryDetails

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

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

    private var _initiateImageImportFromGallery = MutableLiveData<Boolean?>()
    val initiateImageImportFromGallery: LiveData<Boolean?>
        get() = _initiateImageImportFromGallery

    private var _showSnackbarEventMemoryPhotosDeleted = MutableLiveData<Boolean>()
    val showSnackbarEventMemoryPhotosDeleted: LiveData<Boolean>
        get() = _showSnackbarEventMemoryPhotosDeleted

    private val _openDialogFragment = MutableLiveData<Boolean?>()
    val openDialogFragment: LiveData<Boolean?>
        get() = _openDialogFragment

    fun doneShowingSnackbarMemoryPhotosDeleted() {
        _showSnackbarEventMemoryPhotosDeleted.value = false
    }

    fun doneShowingDialogFragment() {
        _openDialogFragment.value = false
    }

    init {
        memory.addSource(database.getMemoryWithId(memoryKey), memory::setValue)
    }

    fun getMemory() = memory

    fun onUpdateMemory() {
        uiScope.launch {
            val oldMemory = memory.value ?: return@launch
            oldMemory.memoryDescription = memoryDescription.value.toString()
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

    }

    fun onNewMemoryPhotoClicked() {
        _initiateImageImportFromGallery.value = true
    }

    fun onDescriptionTextClicked() {
        _openDialogFragment.value = true
    }

    fun onClear() {
        uiScope.launch {
            clearMemoryPhotos(memoryKey)

            _showSnackbarEventMemoryPhotosDeleted.value = true
        }
    }

    suspend fun clearMemoryPhotos(memoryKey: Long) {
        withContext(Dispatchers.IO) {
            database.clearAllPhotosFromMemory(memoryKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}