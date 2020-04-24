package com.example.traveljournal.experience

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentExperienceBinding
import com.example.traveljournal.databinding.FragmentNewExperienceBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.fragment_experience.*
import kotlinx.android.synthetic.main.fragment_new_experience.*
import java.util.*

class NewExperienceFragment: Fragment(), PlaceSelectionListener {

    private lateinit var newExperienceViewModel: NewExperienceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.create_experience)

        val binding: FragmentNewExperienceBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_experience,
            container,
            false)

        val application = requireNotNull(this.activity).application

        val arguments = NewExperienceFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = NewExperienceViewModelFactory(arguments.journeyKey, dataSource)

        newExperienceViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(NewExperienceViewModel::class.java)

        binding.newExperienceViewModel = newExperienceViewModel

        newExperienceViewModel.navigateToJourneyDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { journeyKey ->
            journeyKey?.let {
                this.findNavController().navigate(
                    NewExperienceFragmentDirections.actionNewExperienceDestinationToJourneyDetailsDestination(journeyKey))
                newExperienceViewModel.doneNavigatingToJourneyDetails()
            }
        })

        if (!Places.isInitialized()) {
            this.context?.let { Places.initialize(it, getString(R.string.apiKey), Locale.US) }
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.experience_autocomplete_fragment)
                as? AutocompleteSupportFragment
        autocompleteFragment?.setOnPlaceSelectedListener(this)
        autocompleteFragment!!.setHint(getString(R.string.experience_example))
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS))

        val clearButton = autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
        clearButton?.setOnClickListener {
            autocompleteFragment.setText("")
            newExperienceViewModel.selectedExperiencePlaceName.value = null
            newExperienceViewModel.selectedExperiencePlaceAddress.value = null
        }

        binding.experienceNameEditText.afterTextChanged { experienceName ->
            newExperienceViewModel.experienceName.value = experienceName
        }

        return binding.root
    }

    override fun onPlaceSelected(p0: Place) {
        newExperienceViewModel.selectedExperiencePlaceName.value = p0.name
        newExperienceViewModel.selectedExperiencePlaceAddress.value = p0.address
    }

    override fun onError(p0: Status) {
        Log.i("ERROR", "An error occurred: " + p0)
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}