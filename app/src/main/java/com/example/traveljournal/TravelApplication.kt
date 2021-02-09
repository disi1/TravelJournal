package com.example.traveljournal

import android.app.Application

class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}