package com.example.traveljournal.journeys

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogRestoreBinding

class RestoreDialogFragment(val journeysViewModel: JourneysViewModel, val backupPath: String):DialogFragment() {
    private lateinit var selectFolderButton: Button
    private lateinit var restoreButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogRestoreBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_restore,
            container,
            false
        )

        binding.journeysViewModel = journeysViewModel

        selectFolderButton = binding.selectFolderButton
        restoreButton = binding.restoreButton
        binding.backupPathText.text = backupPath

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreButton.setOnClickListener {
            journeysViewModel.onDialogRestoreButtonClicked()
            journeysViewModel.doneShowingRestoreDialogFragment()
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