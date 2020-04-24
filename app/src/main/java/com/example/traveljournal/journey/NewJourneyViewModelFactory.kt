package com.example.traveljournal.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traveljournal.database.TravelDatabaseDao
import java.lang.IllegalArgumentException
import javax.sql.CommonDataSource

class NewJourneyViewModelFactory(
        private val dataSource: TravelDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override  fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NewJourneyViewModel::class.java)) {
            return NewJourneyViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}