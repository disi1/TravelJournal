package com.example.traveljournal.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.settings)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.backgroundColor))
        )

        val binding: FragmentSettingsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings, container, false
        )

        val application = requireNotNull(this.activity).application

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = SettingsViewModelFactory(dataSource, application)

        val settingsViewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)

        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this

        settingsViewModel.showDataDeletedSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_journeys_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                settingsViewModel.doneShowingDataDeletedSnackbar()
            }
        })

        settingsViewModel.openBackupDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = BackupDialogFragment(settingsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("backup_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "backup_dialog")
            }
        })

        settingsViewModel.openDeleteDataDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = DeleteAllDataDialogFragment(settingsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("delete_data_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "delete_data_dialog")
            }
        })

        settingsViewModel.navigateToJourneys.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsDestinationToJourneysDestination())
                settingsViewModel.doneNavigatingToJourneys()
            }
        })

        settingsViewModel.isAlarmOn.observe(viewLifecycleOwner, Observer {
            binding.reminderFrequencySpinner.isEnabled = it != true
        })

        createChannel(getString(R.string.backup_notification_channel_id), getString(R.string.backup_notification_channel_name))

        return binding.root
    }

    private fun createChannel(channelId: String, channelName: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    setShowBadge(true)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.description = "TravelCompanion backup time"

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}