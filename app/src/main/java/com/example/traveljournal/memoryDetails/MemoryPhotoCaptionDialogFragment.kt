package com.example.traveljournal.memoryDetails

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.databinding.FragmentDialogMemoryPhotoCaptionBinding
import com.example.traveljournal.experienceDetails.ExperienceDescriptionDialogFragment
import com.google.android.material.textfield.TextInputEditText

class MemoryPhotoCaptionDialogFragment(
    val memoryDetailsViewModel: MemoryDetailsViewModel,
    val memoryPhoto: MemoryPhoto
) : DialogFragment() {

    private lateinit var memoryPhotoCaptionEditText: TextInputEditText
    private lateinit var okButton: Button
    private lateinit var cancelButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogMemoryPhotoCaptionBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_memory_photo_caption,
            container,
            false
        )

        binding.memoryPhoto = memoryPhoto

        memoryPhotoCaptionEditText = binding.memoryPhotoCaptionEditText
        okButton = binding.okButton
        cancelButton = binding.cancelButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        okButton.setOnClickListener {
            val dialogListener = targetFragment as DialogListener
            dialogListener.onFinishEditDialog(memoryPhotoCaptionEditText.text.toString())
            memoryDetailsViewModel.doneShowingMemoryPhotoCaptionDialogFragment()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
            memoryDetailsViewModel.doneShowingMemoryPhotoCaptionDialogFragment()
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