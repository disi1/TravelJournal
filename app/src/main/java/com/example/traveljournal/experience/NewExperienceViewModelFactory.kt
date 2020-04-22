package com.example.traveljournal.experience

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao

class NewExperienceViewModelFactory(
    private val journeyKey: Long,
//    private val experienceKey: Long,
    private val dataSource: TravelDatabaseDao
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override  fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NewExperienceViewModel::class.java)) {
//            return NewExperienceViewModel(journeyKey, experienceKey, dataSource) as T
            return NewExperienceViewModel(journeyKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}