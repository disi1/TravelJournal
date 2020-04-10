package com.example.traveljournal.journeyDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao
import java.lang.IllegalArgumentException

class JourneyDetailsViewModelFactory(
    private val journeyKey: Long = 0L,
    private val dataSource: TravelDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JourneyDetailsViewModel::class.java)) {
            return JourneyDetailsViewModel(journeyKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}