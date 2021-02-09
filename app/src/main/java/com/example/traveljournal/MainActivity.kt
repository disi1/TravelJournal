package com.example.traveljournal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_journeys)

        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        val navController = this.findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.journeys_destination))

        if (!AppPreferences.firstTimeOpeningApp) {
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.navigation)
            graph.startDestination = R.id.journeys_destination

            navController.graph = graph
        }

        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}
