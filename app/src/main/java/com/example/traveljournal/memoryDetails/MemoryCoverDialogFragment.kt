package com.example.traveljournal.memoryDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogMemoryCoverBinding

class MemoryCoverDialogFragment(val memoryDetailsViewModel: MemoryDetailsViewModel) :
    DialogFragment() {

    private lateinit var changeCoverPhotoButton: ImageButton
    private lateinit var closeCoverPhotoDialogButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDialogMemoryCoverBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_memory_cover,
            container,
            false
        )

        binding.memoryDetailsViewModel = memoryDetailsViewModel
        changeCoverPhotoButton = binding.changeCoverButton
        closeCoverPhotoDialogButton = binding.closeCoverPhotoButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeCoverPhotoButton.setOnClickListener {
            memoryDetailsViewModel.onChangeCoverPhotoClicked()
            memoryDetailsViewModel.doneImportingImageFromGallery()
            memoryDetailsViewModel.onCloseMemoryCoverDialog()
            dismiss()
        }

        closeCoverPhotoDialogButton.setOnClickListener {
            memoryDetailsViewModel.onCloseMemoryCoverDialog()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}