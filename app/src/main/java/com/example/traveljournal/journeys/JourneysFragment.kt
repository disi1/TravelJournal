package com.example.traveljournal.journeys


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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

        val backupPath = requireContext().getExternalFilesDir(null)!!.path + "/Backup/"

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

        journeysViewModel.navigateToJourneyDetails.observe(viewLifecycleOwner, Observer { journeyKey ->
            journeyKey?.let {
                val destination = JourneysFragmentDirections.actionJourneysDestinationToJourneyDetailsDestination(journeyKey)
                this.findNavController().navigate(destination)
                journeysViewModel.onJourneyDetailsNavigated()
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

        journeysViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_journeys_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbar()
            }
        })

        journeysViewModel.openBackupDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = BackupDialogFragment(journeysViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("backup_methods_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "backup_methods_dialog")
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

        journeysViewModel.openRestoreGuideDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = RestoreGuideDialogFragment(journeysViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("restore_guide_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "restore_guide_dialog")
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

        val deleteAllDataMenu = menu.findItem(R.id.delete_all_data_menu)
        val spannableString = SpannableString(deleteAllDataMenu.title.toString())
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannableString.length, 0)
        deleteAllDataMenu.title = spannableString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_data_menu) {
            val dialogFragment = DeleteAllDataDialogFragment(journeysViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("backup_methods_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "backup_methods_dialog")

            return true
        }

        if(id == R.id.backup_data_menu) {
            journeysViewModel.onBackupButtonClicked()
            return true
        }

        if(id == R.id.restore_data_menu) {
            journeysViewModel.onRestoreButtonClicked()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun triggerRestart(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Runtime.getRuntime().exit(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9990) {
            journeysViewModel.onBackupButtonClicked()
        }
    }
}
