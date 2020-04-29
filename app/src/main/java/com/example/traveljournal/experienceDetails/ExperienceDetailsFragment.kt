package com.example.traveljournal.experienceDetails


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentExperienceDetailsBinding
import com.google.android.material.snackbar.Snackbar

class ExperienceDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.experience_details)

        val binding: FragmentExperienceDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_experience_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = ExperienceDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = ExperienceDetailsViewModelFactory(arguments.experienceKey, dataSource)

        val experienceDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ExperienceDetailsViewModel::class.java)

        binding.experienceDetailsViewModel = experienceDetailsViewModel
        binding.lifecycleOwner = this

        val adapter = MemoryAdapter()
        binding.memoriesList.adapter = adapter

        experienceDetailsViewModel.memories.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        binding.experienceDescriptionTextInput.afterTextChanged { experienceDescription ->
            experienceDetailsViewModel.experienceDescription.value = experienceDescription
        }

        experienceDetailsViewModel.showSnackbarEventExperienceUpdated.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.experience_updated_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                experienceDetailsViewModel.doneShowingSnackbarExperienceUpdated()
            }
        })

        experienceDetailsViewModel.navigateToNewMemory.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToNewMemoryDestination(experienceKey))
                experienceDetailsViewModel.doneNavigatingToNewMemory()
            }
        })

        return binding.root
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
