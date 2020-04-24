package com.example.traveljournal.journeyDetails

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
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
    private lateinit var journeyDetailsViewModel: JourneyDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.journey)

        val binding: FragmentJourneyDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journey_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = JourneyDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = JourneyDetailsViewModelFactory(arguments.journeyKey, dataSource)

        journeyDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(JourneyDetailsViewModel::class.java)

        binding.journeyDetailsViewModel = journeyDetailsViewModel
        binding.lifecycleOwner = this

        val adapter = ExperienceAdapter(ExperienceListener {
            experienceId -> journeyDetailsViewModel.onExperienceClicked(experienceId)
        })
        binding.experiencesList.adapter = adapter

        journeyDetailsViewModel.experiences.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
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

        journeyDetailsViewModel.navigateToExperienceDetails.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToExperienceDetailsDestination(experienceKey))
                journeyDetailsViewModel.doneNavigatingToExperienceDetails()
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

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journey_details_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.clear_all_experiences_menu) {
            journeyDetailsViewModel.onClear()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}