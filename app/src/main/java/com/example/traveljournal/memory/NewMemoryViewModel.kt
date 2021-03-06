package com.example.traveljournal.memory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*

class NewMemoryViewModel(
    private val experienceKey: Long = 0L,
    val database: TravelDatabaseDao
) : ViewModel() {

    val memoryName = MutableLiveData<String>()

    val memoryDescription = MutableLiveData<String>()

    val memoryTimestamp = MutableLiveData<Long>()

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _chooseDateTextViewClicked = MutableLiveData<Boolean?>()
    val chooseDateTextViewClickedClicked: LiveData<Boolean?>
        get() = _chooseDateTextViewClicked

    fun onChooseDateTextViewClicked() {
        _chooseDateTextViewClicked.value = true
    }

    private val _navigateToExperienceDetails = MutableLiveData<Long>()
    val navigateToExperienceDetails: LiveData<Long>
        get() = _navigateToExperienceDetails

    fun doneNavigatingToExperienceDetails() {
        _navigateToExperienceDetails.value = null
    }

    fun onCreateMemory() {
        uiScope.launch {
            val memory = Memory(
                experienceHostId = experienceKey,
                memoryTimestamp = memoryTimestamp.value!!.toLong()
            )

            memory.memoryName = memoryName.value.toString()
            if (memoryDescription.value == null) {
                memory.memoryDescription = ""
            } else {
                memory.memoryDescription = memoryDescription.value.toString()
            }

            insertMemory(memory)

            _navigateToExperienceDetails.value = experienceKey
        }
    }

    private suspend fun insertMemory(memory: Memory) {
        withContext(Dispatchers.IO) {
            database.insertMemory(memory)
        }
    }

    fun onCancelNewMemory() {
        _navigateToExperienceDetails.value = experienceKey
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}