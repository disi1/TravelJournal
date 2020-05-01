package com.example.traveljournal.memoryDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao

class MemoryDetailsViewModelFactory(
    private val memoryKey: Long = 0L,
    private val dataSource: TravelDatabaseDao
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryDetailsViewModel::class.java)) {
            return MemoryDetailsViewModel(memoryKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
