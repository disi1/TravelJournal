package com.example.traveljournal.journeyDetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneyDetailsBinding
import com.google.android.material.snackbar.Snackbar

class JourneyDetailsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.my_journeys)

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

        val adapter = ExperienceAdapter()
        binding.experiencesList.adapter = adapter

        journeyDetailsViewModel.experiences.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        journeyDetailsViewModel.navigateToJourneys.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToJourneysDestination())
                journeyDetailsViewModel.doneNavigating()
            }
        })

        journeyDetailsViewModel.navigateToNewExperience.observe(viewLifecycleOwner, Observer { journeyKey ->
            journeyKey?.let {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToNewExperienceDestination(journeyKey))
                journeyDetailsViewModel.doneNavigatingToNewExperience()
            }
        })

        journeyDetailsViewModel.showSnackbarEventExpDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_experiences_message,
                        journeyDetailsViewModel.getJourney().value?.placeName),
                        Snackbar.LENGTH_SHORT
                    ).show()
                journeyDetailsViewModel.doneShowingSnackbarExpDeleted()
            }
        })

        return binding.root
    }



}