package com.example.traveljournal.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao

class NewMemoryViewModelFactory (
    private val experienceKey: Long,
    private val dataSource: TravelDatabaseDao
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override  fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NewMemoryViewModel::class.java)) {
            return NewMemoryViewModel(experienceKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}