package com.example.traveljournal.journeys


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.MainActivity
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneysBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.waitForTransition
import com.google.android.material.snackbar.Snackbar

class JourneysFragment : Fragment() {
    private lateinit var journeysViewModel: JourneysViewModel
    private lateinit var binding: FragmentJourneysBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(android.R.attr.background.toDrawable())

        val intent = (activity as AppCompatActivity).intent

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journeys, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = JourneysViewModelFactory(dataSource, application)

        journeysViewModel = ViewModelProviders.of(this, viewModelFactory).get(JourneysViewModel::class.java)

        binding.journeysViewModel = journeysViewModel
        binding.lifecycleOwner = this

        if(intent.data != null) {
            journeysViewModel.backupFilePath.value = intent.data.toString()
            intent.data = null
            journeysViewModel.onRestoreMechanismInitialized()
        }

        val manager = GridLayoutManager(activity, 2)
        binding.journeysList.layoutManager = manager

        val adapter = JourneyAdapter(
            journeysViewModel.journeys.value
        )

        binding.journeysList.adapter = adapter

        journeysViewModel.journeys.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(adapter.journeysList == null) {
                    adapter.journeysList = it
                }
                adapter.submitList(it)
                if(it.isNotEmpty()) {
                    binding.imageView.visibility = ConstraintLayout.GONE
                    binding.searchView.visibility = ConstraintLayout.VISIBLE
                } else {
                    binding.imageView.visibility = ConstraintLayout.VISIBLE
                    binding.searchView.visibility = ConstraintLayout.GONE
                }
            }
        })

        binding.searchView.isFocusable = false

        binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        journeysViewModel.navigateToNewJourney.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    JourneysFragmentDirections
                        .actionJourneysDestinationToNewJourneyDestination())
                journeysViewModel.doneNavigating()
            }
        })

        journeysViewModel.navigateToSettings.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    JourneysFragmentDirections
                        .actionJourneysDestinationToSettingsDestination())
                journeysViewModel.onDoneNavigatingToSettings()
            }
        })

        journeysViewModel.openRestoreDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = RestoreDialogFragment(journeysViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("restore_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "restore_dialog")
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        waitForTransition(binding.journeysList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journeys_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.settings_menu) {
            journeysViewModel.onNavigateToSettings()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
