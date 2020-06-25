package com.example.traveljournal.settings

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogDeleteAllDataBinding
import com.example.traveljournal.getBackupPath
import java.io.File

class DeleteAllDataDialogFragment(private val settingsViewModel: SettingsViewModel): DialogFragment() {

    private lateinit var deleteButton: Button
    private lateinit var cancelButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogDeleteAllDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_delete_all_data,
            container,
            false
        )

        cancelButton = binding.cancelButton
        deleteButton = binding.deleteButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteButton.setOnClickListener {
            settingsViewModel.onDeleteData(getBackupPath(requireContext()))
            settingsViewModel.doneShowingDeleteDataDialogFragment()
            dismiss()
        }

        cancelButton.setOnClickListener {
            settingsViewModel.doneShowingDeleteDataDialogFragment()
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