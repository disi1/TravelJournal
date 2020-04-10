package com.example.traveljournal.journeys


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneysBinding
import com.google.android.material.snackbar.Snackbar

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
        
        val manager = GridLayoutManager(activity, 2)
        binding.journeysList.layoutManager = manager

        val adapter = JourneyAdapter(JourneyListener {
            journeyId -> journeysViewModel.onJourneyClicked(journeyId)
        })
        binding.journeysList.adapter = adapter

        journeysViewModel.navigateToJourneyDetails.observe(viewLifecycleOwner, Observer { journey ->
            journey?.let {
                this.findNavController().navigate(
                    JourneysFragmentDirections.actionJourneysDestinationToJourneyDetailsDestination(journey))
                journeysViewModel.onJourneyDetailsNavigated()
            }
        })

        journeysViewModel.journeys.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.lifecycleOwner = this

        journeysViewModel.navigateToNewJourney.observe(viewLifecycleOwner, Observer { journey ->
            journey?.let {
                this.findNavController().navigate(
                    JourneysFragmentDirections
                        .actionJourneysDestinationToNewJourneyDestination(journey.journeyId))
                journeysViewModel.doneNavigating()
            }
        })

        journeysViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }
}
