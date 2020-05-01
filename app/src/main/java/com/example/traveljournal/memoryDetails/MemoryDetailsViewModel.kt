package com.example.traveljournal.memoryDetails

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Memory
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

    init {
        memory.addSource(database.getMemoryWithId(memoryKey), memory::setValue)
    }

    fun getMemory() = memory

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}