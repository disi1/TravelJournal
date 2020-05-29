package com.example.traveljournal.memoryDetails

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
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

        memoryDetailsViewModel.memoryPhotoDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                dismiss()
                memoryDetailsViewModel.doneDeletingMemoryPhoto()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closePhotoButton.setOnClickListener {
            memoryDetailsViewModel.onCloseMemoryPhotoDialog()
            dismiss()
        }

        deletePhotoButton.setOnClickListener {
            val dialogFragment = DeletePhotoDialogFragment(memoryPhoto, memoryDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_photo_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_photo_dialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}