package com.example.traveljournal.journeys

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogBackupMethodsBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getCurrentDate
import com.example.traveljournal.getCurrentDateAndTime
import java.io.File
import java.nio.file.spi.FileSystemProvider

class BackupMethodsDialogFragment(val journeysViewModel: JourneysViewModel): DialogFragment() {

    private lateinit var localStorageGroupButton: ConstraintLayout
    private lateinit var otherStorageGroupButton: ConstraintLayout
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogBackupMethodsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_backup_methods,
            container,
            false
        )

        binding.journeysViewModel = journeysViewModel

        localStorageGroupButton = binding.localStorageGroup
        otherStorageGroupButton = binding.otherStorageGroup
        cancelButton = binding.cancelButton

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localStorageGroupButton.setOnClickListener {
            journeysViewModel.onDialogBackupMethodsLocalStorageClicked()
        }

        otherStorageGroupButton.setOnClickListener {
            val zipBackupFile = getBackupPath(requireContext()) + "TravelJournalBackup.zip"

            journeysViewModel.onExternalStorageBackup(getBackupPath(requireContext()), zipBackupFile, requireContext())

            backUpDataToEmail(zipBackupFile, requireContext())

            journeysViewModel.doneShowingBackupMethodsDialogFragment()
            dismiss()
        }

        cancelButton.setOnClickListener {
            journeysViewModel.doneShowingBackupMethodsDialogFragment()
            dismiss()
        }

        journeysViewModel.openBackupMethodsDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == false) {
                dismiss()
            }
        })
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        val display: Display = window!!.windowManager.defaultDisplay

        display.getSize(size)
        window.setLayout((size.x * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setLayout((size.y * 0.50).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)

        super.onResume()
    }

    private fun backUpDataToEmail(backupZipFilePath: String , context: Context) {
        val backupZipFile = File(backupZipFilePath)
        val contentUri: Uri = getUriForFile(context, "com.example.traveljournal.fileprovider", backupZipFile)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Travel Journal Backup")
            putExtra(Intent.EXTRA_TEXT, "You backed your data up at ${getCurrentDateAndTime()}.")
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/zip"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}