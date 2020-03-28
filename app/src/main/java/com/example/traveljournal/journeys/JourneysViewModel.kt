package com.example.traveljournal.journeys

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.database.TravelDatabaseDao

class JourneysViewModel(
    val database: TravelDatabaseDao,
    application: Application) : AndroidViewModel(application) {
    fun newJourney() {

    }
}