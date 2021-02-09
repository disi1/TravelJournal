package com.example.traveljournal.experienceDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao

class ExperienceDetailsViewModelFactory(
    private val experienceKey: Long = 0L,
    private val dataSource: TravelDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExperienceDetailsViewModel::class.java)) {
            return ExperienceDetailsViewModel(experienceKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}