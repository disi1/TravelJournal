package com.example.traveljournal.journeys


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneysBinding

class JourneysFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.myJourneys)

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentJourneysBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journeys, container, false)
        // get a reference to the application that this fragment is attached to, to pass in to the ViewModelFactory provider
        val application = requireNotNull(this.activity).application
        // get a reference to the data source via a reference to the Dao
        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = JourneysViewModelFactory(dataSource, application)
        val journeysViewModel = ViewModelProviders.of(this, viewModelFactory).get(JourneysViewModel::class.java)
        binding.journeysViewModel = journeysViewModel
        binding.setLifecycleOwner(this)

        binding.newJourneyButton.setOnClickListener {
            this.findNavController()
                .navigate(JourneysFragmentDirections.actionJourneysDestinationToNewJourneyDestination())
        }
        return binding.root
    }
}
