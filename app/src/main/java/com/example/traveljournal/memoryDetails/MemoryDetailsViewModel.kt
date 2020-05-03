package com.example.traveljournal.memoryDetails

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MemoryDetailsViewModel(
    private val memoryKey: Long = 0L,
    dataSource: TravelDatabaseDao
): ViewModel() {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val memory = MediatorLiveData<Memory>()

    val memoryPhotos = database.getAllPhotosFromMemory(memoryKey)

    init {
        memory.addSource(database.getMemoryWithId(memoryKey), memory::setValue)
    }

    fun getMemory() = memory

    fun onMemoryPhotoClicked(memoryPhoto: MemoryPhoto) {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}