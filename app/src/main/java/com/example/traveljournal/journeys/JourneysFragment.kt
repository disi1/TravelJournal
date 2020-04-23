package com.example.traveljournal.journeys


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneysBinding
import com.google.android.material.snackbar.Snackbar


class JourneysFragment : Fragment() {
    private lateinit var journeysViewModel: JourneysViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.my_journeys)

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentJourneysBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journeys, container, false)

        // get a reference to the application that this fragment is attached to, to pass in to the ViewModelFactory provider
        val application = requireNotNull(this.activity).application

        // get a reference to the data source via a reference to the Dao
        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = JourneysViewModelFactory(dataSource, application)

        journeysViewModel = ViewModelProviders.of(this, viewModelFactory).get(JourneysViewModel::class.java)

        binding.journeysViewModel = journeysViewModel
        
        val manager = GridLayoutManager(activity, 2)
        binding.journeysList.layoutManager = manager

        val adapter = JourneyAdapter(
            JourneyListener {
                journeyId -> journeysViewModel.onJourneyClicked(journeyId)},
            JourneyLongClickListener {
//                journey ->  Toast.makeText(context, journey.placeName, Toast.LENGTH_LONG).show()})
                    journey ->  Toast.makeText(context, "Journey long-clicked", Toast.LENGTH_LONG).show()})
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
                    getString(R.string.cleared_journeys_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbar()
            }
        })

        journeysViewModel.showSnackBarEventJourneyDeleted.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.deleted_journey_message, "Honolulu"),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbarJourneyDeleted()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journeys_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.clear_all_menu) {
            journeysViewModel.onClear()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

//    private fun setRecyclerViewItemTouchListener(journey: Journey) {
//        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(
//            0,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                Toast.makeText(context, journey.placeName, Toast.LENGTH_LONG).show()
////                journeysViewModel.onDeleteJourney(journey)
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
//        itemTouchHelper.attachToRecyclerView()
//    }
}
