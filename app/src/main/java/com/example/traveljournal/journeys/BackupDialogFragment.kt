package com.example.traveljournal.journeys

import android.Manifest
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogBackupBinding

class BackupDialogFragment(val journeysViewModel: JourneysViewModel, val backupPath: String): DialogFragment() {

    private lateinit var selectFolderButton: Button
    private lateinit var backupButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogBackupBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_backup,
            container,
            false
        )

        binding.journeysViewModel = journeysViewModel

        selectFolderButton = binding.selectFolderButton
        backupButton = binding.backupButton
        binding.backupPathText.text = backupPath

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backupButton.setOnClickListener {
            journeysViewModel.onDialogBackupButtonClicked()
            journeysViewModel.doneShowingBackupDialogFragment()
            dismiss()
        }
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
}