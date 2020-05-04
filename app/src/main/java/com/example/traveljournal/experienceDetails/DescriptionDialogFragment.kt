package com.example.traveljournal.experienceDetails

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogExperienceDescriptionBinding
import com.google.android.material.textfield.TextInputEditText


class DescriptionDialogFragment(val experienceDetailsViewModel: ExperienceDetailsViewModel): DialogFragment() {

    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogExperienceDescriptionBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_experience_description,
            container,
            false
        )

        binding.experienceDetailsViewModel = experienceDetailsViewModel

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
        // Store dimensions of the screen in `size`
        val display: Display = window!!.getWindowManager().getDefaultDisplay()
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        window!!.setLayout((size.x * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        super.onResume()
    }

    interface DialogListener {
        fun onFinishEditDialog(inputText: String)
    }
}