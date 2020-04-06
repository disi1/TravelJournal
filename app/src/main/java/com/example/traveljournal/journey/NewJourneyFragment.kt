package com.example.traveljournal.journey

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentNewJourneyBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*


class NewJourneyFragment : Fragment(), PlaceSelectionListener {

    private lateinit var newJourneyViewModel: NewJourneyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.createJourney)

        val binding: FragmentNewJourneyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_journey, container, false
        )

        val application = requireNotNull(this.activity).application

        val arguments = NewJourneyFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = NewJourneyViewModelFactory(arguments.journeyKey, dataSource)

        newJourneyViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(NewJourneyViewModel::class.java)

        binding.newJourneyViewModel = newJourneyViewModel

        newJourneyViewModel.navigateToJourneys.observe(this, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    NewJourneyFragmentDirections.actionNewJourneyDestinationToJourneysDestination())
                newJourneyViewModel.doneNavigating()
            }
        })

//        setHasOptionsMenu(true)

        if (!Places.isInitialized()) {
            this.context?.let { Places.initialize(it, getString(R.string.apiKey), Locale.US) }
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as? AutocompleteSupportFragment
        autocompleteFragment?.setOnPlaceSelectedListener(this)
        autocompleteFragment!!.setHint(getString(R.string.destinationExample))
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS))

        val clearButton = autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
        clearButton?.setOnClickListener { view: View? ->
            autocompleteFragment.setText("")
            newJourneyViewModel.selectedPlaceName.value = null
        }

        return binding.root
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater?.inflate(R.menu.create_journey_menu, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.getItemId()
//
//        if (id == R.id.createJourneyButton) {
//            Toast.makeText(this.context, "Item Clicked", Toast.LENGTH_LONG).show()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    @ExperimentalStdlibApi
    override fun onPlaceSelected(p0: Place) {
        Log.i("JO", "Place: " + p0.name + ", " + p0.id )
        Log.i("JO", "Place: $p0")
        newJourneyViewModel.selectedPlaceName.value = p0.name
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context,""+status.toString(),Toast.LENGTH_LONG).show()
        Log.i("ERROR", "An error occurred: " + status)
    }
}
