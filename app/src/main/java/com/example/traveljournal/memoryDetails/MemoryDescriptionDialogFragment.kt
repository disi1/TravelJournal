package com.example.traveljournal.memoryDetails

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogMemoryDescriptionBinding
import com.google.android.material.textfield.TextInputEditText

class MemoryDescriptionDialogFragment(val memoryDetailsViewModel: MemoryDetailsViewModel): DialogFragment() {
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogMemoryDescriptionBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_memory_description,
            container,
            false
        )

        binding.memoryDetailsViewModel = memoryDetailsViewModel

        descriptionEditText = binding.descriptionEditText
        doneButton = binding.doneButton
        cancelButton = binding.cancelButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doneButton.setOnClickListener {
            val dialogListener = targetFragment as DialogListener
            dialogListener.onFinishEditDialog(descriptionEditText.text.toString())
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

    interface DialogListener {
        fun onFinishEditDialog(inputText: String)
    }
}