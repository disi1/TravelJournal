package com.example.traveljournal.memoryDetails

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.databinding.FragmentDialogMemoryPhotoBinding

class MemoryPhotoDialogFragment(val memoryPhoto: MemoryPhoto, val memoryDetailsViewModel: MemoryDetailsViewModel): DialogFragment() {
    private lateinit var closePhotoButton: ImageButton
    private lateinit var deletePhotoButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDialogMemoryPhotoBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_memory_photo,
            container,
            false
        )

        binding.memoryPhoto = memoryPhoto
        closePhotoButton = binding.closePhotoButton
        deletePhotoButton = binding.deletePhotoButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deletePhotoButton.setOnClickListener {

        }

        closePhotoButton.setOnClickListener {
            memoryDetailsViewModel.onCloseMemoryPhotoDialog()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}