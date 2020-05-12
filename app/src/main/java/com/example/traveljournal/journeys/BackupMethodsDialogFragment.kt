package com.example.traveljournal.journeys

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogBackupMethodsBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getCurrentDate
import com.example.traveljournal.getCurrentDateAndTime

class BackupMethodsDialogFragment(val journeysViewModel: JourneysViewModel): DialogFragment() {

    private lateinit var localStorageGroupButton: ConstraintLayout
    private lateinit var otherStorageGroupButton: ConstraintLayout

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localStorageGroupButton.setOnClickListener {
            journeysViewModel.onDialogBackupMethodsLocalStorageClicked()
        }

        otherStorageGroupButton.setOnClickListener {
            val zipBackupFile = getBackupPath(context!!) + "TravelJournalBackup_${getCurrentDate()}.zip"

            journeysViewModel.onZipFiles(getBackupPath(context!!), zipBackupFile)

            backUpDataToEmail(zipBackupFile, context!!)

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

    private fun backUpDataToEmail(zipFilePath: String , context: Context) {
        val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, "Travel Journal Backup")
            putExtra(Intent.EXTRA_TEXT, "You backed your data up at ${getCurrentDateAndTime()}.")
            putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$zipFilePath"))
            type = "application/zip"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_LONG).show()
        }
    }
}