package com.example.traveljournal.memoryDetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.backupPhoto
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentMemoryDetailsBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getRealPath
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MemoryDetailsFragment: Fragment(), MemoryDescriptionDialogFragment.DialogListener {
    private lateinit var memoryDetailsViewModel: MemoryDetailsViewModel
    private lateinit var backupPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.memory_details)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_background))

        val binding : FragmentMemoryDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_memory_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = MemoryDetailsFragmentArgs.fromBundle(requireArguments())

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = MemoryDetailsViewModelFactory(arguments.memoryKey, dataSource)

        memoryDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(MemoryDetailsViewModel::class.java)

        binding.memoryDetailsViewModel = memoryDetailsViewModel

        binding.lifecycleOwner = this

        val manager = GridLayoutManager(activity, 3)

        manager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = when (position) {
                0 -> 3
                else -> 1
            }
        }

        binding.memoryPhotosGrid.layoutManager = manager

        val adapter = MemoryPhotoGridAdapter(MemoryPhotoListener {
            memoryPhoto ->  memoryDetailsViewModel.onMemoryPhotoClicked(memoryPhoto)
        }, memoryDetailsViewModel)

        binding.memoryPhotosGrid.adapter = adapter

        backupPhotoPath = getBackupPath(requireContext()) + "Media/"

        memoryDetailsViewModel.openPhotoDialogFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                val dialogFragment = MemoryPhotoDialogFragment(it, memoryDetailsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("photoDialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "photoDialog")
            }
        })

        memoryDetailsViewModel.openCoverPhotoDialogFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val dialogFragment = MemoryCoverDialogFragment(memoryDetailsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("photoDialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "photoDialog")
            }
        })

        memoryDetailsViewModel.memoryDescription.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.notifyItemChanged(0, null)
            }
        })

        memoryDetailsViewModel.memoryPhotos.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })

        memoryDetailsViewModel.initiateImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestStoragePermission(9990)
            }
        })

        memoryDetailsViewModel.initiateCoverImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestStoragePermission(9900)
            }
        })

        memoryDetailsViewModel.showSnackbarEventMemoryPhotosDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_memory_photos_message,
                        memoryDetailsViewModel.getMemory().value?.memoryName),
                    Snackbar.LENGTH_SHORT
                ).show()
                memoryDetailsViewModel.doneShowingSnackbarMemoryPhotosDeleted()
            }
        })

        memoryDetailsViewModel.openDescriptionDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = MemoryDescriptionDialogFragment(memoryDetailsViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "dialog")
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.memory_details_overflow_menu, menu)

        val deleteAllExperiencesMenu = menu.findItem(R.id.delete_all_memory_photos_menu)
        val spannableString = SpannableString(deleteAllExperiencesMenu.title.toString())
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannableString.length, 0)
        deleteAllExperiencesMenu.title = spannableString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_memory_photos_menu) {
            val dialogFragment = DeleteAllPhotosDialogFragment(memoryDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("backup_methods_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "backup_methods_dialog")

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestStoragePermission(requestCode: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                when(requestCode) {
                    9990 -> requestPermissions(permission, requestCode)
                    9900 -> requestPermissions(permission, requestCode)
                }
            } else {
                when(requestCode) {
                    9990 -> pickImageFromGallery(requestCode)
                    9900 -> pickImageFromGallery(requestCode)
                }
            }
        }
    }

    private fun pickImageFromGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        when(requestCode) {
            9990 -> startActivityForResult(intent, requestCode)
            9900 -> startActivityForResult(intent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9990 && data != null) {
            val srcFile = getRealPath(data, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            memoryDetailsViewModel.photoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCreateMemoryPhoto()
            memoryDetailsViewModel.doneImportingImageFromGallery()
        }

        if(resultCode == Activity.RESULT_OK && requestCode == 9900 && data != null) {
            val srcFile = getRealPath(data, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            memoryDetailsViewModel.coverPhotoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCoverPhotoChanged()
            memoryDetailsViewModel.doneImportingCoverImageFromGallery()
            memoryDetailsViewModel.onUpdateMemoryCoverPhoto()
        }
    }

    override fun onFinishEditDialog(inputText: String) {
        memoryDetailsViewModel.memoryDescription.value = inputText
        memoryDetailsViewModel.onUpdateMemoryDescription()
        memoryDetailsViewModel.doneShowingDescriptionDialogFragment()
    }
}