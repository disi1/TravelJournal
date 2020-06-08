package com.example.traveljournal.journeys

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogDeleteAllDataBinding
import com.example.traveljournal.getBackupPath
import java.io.File

class DeleteAllDataDialogFragment(val journeysViewModel: JourneysViewModel): DialogFragment() {

    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button

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

        binding.journeysViewModel = journeysViewModel

        cancelButton = binding.cancelButton
        deleteButton = binding.deleteButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteButton.setOnClickListener {
            journeysViewModel.onClear(getBackupPath(requireContext()))
            dismiss()
        }

        cancelButton.setOnClickListener {
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