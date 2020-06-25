package com.example.traveljournal.experienceDetails

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience
import com.example.traveljournal.databinding.FragmentDialogExperienceCoverBinding

class ExperienceCoverDialogFragment(val experienceDetailsViewModel: ExperienceDetailsViewModel): DialogFragment() {
    private lateinit var changeCoverPhotoButton: ImageButton
    private lateinit var closeCoverPhotoDialogButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDialogExperienceCoverBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_experience_cover,
            container,
            false
        )

        if(experienceDetailsViewModel.getExperience().value?.coverPhotoAttributions == "") {
            binding.creditsText.visibility = TextView.GONE
        }

        binding.experienceDetailsViewModel = experienceDetailsViewModel
        binding.creditsText.text = HtmlCompat.fromHtml(getString(R.string.credits_to, experienceDetailsViewModel.getExperience().value?.coverPhotoAttributions), HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.creditsText.movementMethod = LinkMovementMethod.getInstance()
        changeCoverPhotoButton = binding.changeCoverButton
        closeCoverPhotoDialogButton = binding.closeCoverPhotoButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeCoverPhotoButton.setOnClickListener {
            experienceDetailsViewModel.onChangeCoverPhotoClicked()
            experienceDetailsViewModel.doneImportingImageFromGallery()
            experienceDetailsViewModel.onCloseExperienceCoverDialog()
            dismiss()
        }

        closeCoverPhotoDialogButton.setOnClickListener {
            experienceDetailsViewModel.onCloseExperienceCoverDialog()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}