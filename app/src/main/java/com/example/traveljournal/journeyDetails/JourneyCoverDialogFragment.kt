package com.example.traveljournal.journeyDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.FragmentDialogJourneyCoverBinding

class JourneyCoverDialogFragment(val journeyDetailsViewModel: JourneyDetailsViewModel): DialogFragment() {

    private lateinit var changeCoverPhotoButton: ImageButton
    private lateinit var closeCoverPhotoDialogButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDialogJourneyCoverBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_journey_cover,
            container,
            false
        )

        binding.journeyDetailsViewModel = journeyDetailsViewModel
        changeCoverPhotoButton = binding.changeCoverButton
        closeCoverPhotoDialogButton = binding.closeCoverPhotoButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeCoverPhotoButton.setOnClickListener {
            journeyDetailsViewModel.onChangeCoverPhotoClicked()
            journeyDetailsViewModel.doneImportingImageFromGallery()
            journeyDetailsViewModel.onCloseJourneyCoverDialog()
            dismiss()
        }

        closeCoverPhotoDialogButton.setOnClickListener {
            journeyDetailsViewModel.onCloseJourneyCoverDialog()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}