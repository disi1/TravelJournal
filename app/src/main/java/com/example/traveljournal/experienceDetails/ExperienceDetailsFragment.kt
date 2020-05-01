package com.example.traveljournal.experienceDetails


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
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

    private lateinit var experienceDetailsViewModel: ExperienceDetailsViewModel

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

        experienceDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ExperienceDetailsViewModel::class.java)

        binding.experienceDetailsViewModel = experienceDetailsViewModel
        binding.lifecycleOwner = this

        val adapter = MemoryAdapter(MemoryListener {
            memoryId -> experienceDetailsViewModel.onMemoryClicked(memoryId)
        })
        binding.memoriesList.adapter = adapter

        experienceDetailsViewModel.memories.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
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

        experienceDetailsViewModel.showSnackbarEventMemoriesDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_memories_message,
                        experienceDetailsViewModel.getExperience().value?.experienceName),
                    Snackbar.LENGTH_SHORT
                ).show()
                experienceDetailsViewModel.doneShowingSnackbarMemoriesDeleted()
            }
        })

        experienceDetailsViewModel.navigateToNewMemory.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToNewMemoryDestination(experienceKey))
                experienceDetailsViewModel.doneNavigatingToNewMemory()
            }
        })

        experienceDetailsViewModel.navigateToMemoryDetails.observe(viewLifecycleOwner, Observer { memoryKey ->
            memoryKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToMemoryDetailsFragment(memoryKey))
                experienceDetailsViewModel.doneNavigatingToMemoryDetails()
            }
        })

        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.experience_details_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.clear_all_memories_menu) {
            experienceDetailsViewModel.onClear()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
