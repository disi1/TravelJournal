package com.example.traveljournal.journeyDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneyDetailsBinding

class JourneyDetailsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentJourneyDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journey_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = JourneyDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = JourneyDetailsViewModelFactory(arguments.journeyKey, dataSource)

        val journeyDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(JourneyDetailsViewModel::class.java)

        binding.journeyDetailsViewModel = journeyDetailsViewModel
        binding.lifecycleOwner = this

        journeyDetailsViewModel.navigateToJourneys.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToJourneysDestination())
                journeyDetailsViewModel.doneNavigating()
            }
        })

        return binding.root
    }
}