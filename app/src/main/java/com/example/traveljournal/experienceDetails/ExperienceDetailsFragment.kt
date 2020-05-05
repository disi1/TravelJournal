package com.example.traveljournal.experienceDetails


import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentExperienceDetailsBinding
import com.google.android.material.snackbar.Snackbar


class ExperienceDetailsFragment : Fragment(), DescriptionDialogFragment.DialogListener {

    private lateinit var experienceDetailsViewModel: ExperienceDetailsViewModel
    private lateinit var adapter: MemoryAdapter

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

        val manager = LinearLayoutManager(activity)
        binding.memoriesList.layoutManager = manager
        adapter = MemoryAdapter(MemoryListener {
            memoryId -> experienceDetailsViewModel.onMemoryClicked(memoryId)
        }, experienceDetailsViewModel)
        binding.memoriesList.adapter = adapter

        experienceDetailsViewModel.memories.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })

        experienceDetailsViewModel.experienceDescription.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.notifyItemChanged(0, null)
            }
        })

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

        experienceDetailsViewModel.openDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = DescriptionDialogFragment(experienceDetailsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "dialog")
            }
        })

        setHasOptionsMenu(true)

        return binding.root
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

    override fun onFinishEditDialog(inputText: String) {
        experienceDetailsViewModel.experienceDescription.value = inputText
        experienceDetailsViewModel.onUpdateExperience()
        experienceDetailsViewModel.doneShowingDialogFragment()
    }
}
