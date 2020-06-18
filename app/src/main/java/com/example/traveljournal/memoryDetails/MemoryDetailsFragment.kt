package com.example.traveljournal.memoryDetails

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import com.example.traveljournal.*
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentMemoryDetailsBinding
import com.example.traveljournal.experienceDetails.DeleteExperienceDialogFragment
import com.example.traveljournal.experienceDetails.ExperienceDetailsFragmentDirections
import com.google.android.material.snackbar.Snackbar
import java.io.File

private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 8880
private const val READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO = 9990
private const val READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO = 9900

class MemoryDetailsFragment: Fragment(), MemoryDescriptionDialogFragment.DialogListener {
    private lateinit var memoryDetailsViewModel: MemoryDetailsViewModel
    private lateinit var backupPhotoPath: String
    private lateinit var binding: FragmentMemoryDetailsBinding
    private var isRotated: Boolean = false
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_background))

        binding = DataBindingUtil.inflate(
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

        init(binding.addPhotoFromGalleryButton)
        init(binding.addPhotoFromCameraButton)

        binding.addMemoryPhotoButton.setOnClickListener { view ->
            isRotated = rotateFAB(view, !isRotated)
            if(isRotated) {
                showIn(binding.addPhotoFromCameraButton)
                showIn(binding.addPhotoFromGalleryButton)
            } else {
                showOut(binding.addPhotoFromCameraButton)
                showOut(binding.addPhotoFromGalleryButton)
            }
        }

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
                requestReadExternalStoragePermission(READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO)
            }
        })

        memoryDetailsViewModel.initiateCoverImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestReadExternalStoragePermission(READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO)
            }
        })

        memoryDetailsViewModel.initiateImageImportFromCamera.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestWriteExternalStoragePermission(WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
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

        memoryDetailsViewModel.showSnackbarEventMemoryDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.memory_deleted,
                        memoryDetailsViewModel.getMemory().value?.memoryName),
                    Snackbar.LENGTH_SHORT
                ).show()
                memoryDetailsViewModel.doneShowingSnackbarMemoryDeleted()
            }
        })

        memoryDetailsViewModel.navigateToExperienceDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    MemoryDetailsFragmentDirections.actionMemoryDetailsFragmentToExperienceDetailsDestination(experienceKey))
                memoryDetailsViewModel.doneNavigatingToExperienceDetails()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        waitForTransition(binding.memoryImage)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.memory_details_overflow_menu, menu)

        val deleteAllExperiencesMenu = menu.findItem(R.id.delete_all_memory_photos_menu)
        val spannableString = SpannableString(deleteAllExperiencesMenu.title.toString())
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannableString.length, 0)
        deleteAllExperiencesMenu.title = spannableString

        val deleteMemoryMenu = menu.findItem(R.id.delete_memory_menu)
        val spannableDeleteMemoryMenuString = SpannableString(deleteMemoryMenu.title.toString())
        spannableDeleteMemoryMenuString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannableDeleteMemoryMenuString.length, 0)
        deleteMemoryMenu.title = spannableDeleteMemoryMenuString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_memory_photos_menu) {
            val dialogFragment = DeleteAllPhotosDialogFragment(memoryDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_all_memories_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_all_memories_dialog")

            return true
        }

        if(id == R.id.delete_memory_menu) {
            val dialogFragment = DeleteMemoryDialogFragment(memoryDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_memory_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_memory_dialog")

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestReadExternalStoragePermission(requestCode: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                when(requestCode) {
                    READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO -> requestPermissions(permission, requestCode)
                    READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO -> requestPermissions(permission, requestCode)
                }
            } else {
                when(requestCode) {
                    READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO -> pickImageFromGallery(requestCode)
                    READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO -> pickImageFromGallery(requestCode)
                }
            }
        } else {
            when(requestCode) {
                READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO -> pickImageFromGallery(requestCode)
                READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO -> pickImageFromGallery(requestCode)
            }
        }
    }

    private fun requestWriteExternalStoragePermission(requestCode: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_DENIED
                || checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

                requestPermissions(permission, requestCode)
            } else {
                addPhotoFromCamera(requestCode)
            }
        } else {
            addPhotoFromCamera(requestCode)
        }
    }

    private fun pickImageFromGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        when(requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO -> startActivityForResult(intent, requestCode)
            READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO -> startActivityForResult(intent, requestCode)
        }
    }

    private fun addPhotoFromCamera(requestCode: Int) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "${System.currentTimeMillis() / 1000L}")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the TravelCompanion Camera")
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        when(requestCode) {
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> startActivityForResult(cameraIntent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE_PHOTO && data != null) {
            val srcFile = getRealPathForIntentData(data, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            memoryDetailsViewModel.photoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCreateMemoryPhoto()
            memoryDetailsViewModel.doneImportingImageFromGallery()
        }

        if(resultCode == Activity.RESULT_OK && requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE_COVER_PHOTO && data != null) {
            val srcFile = getRealPathForIntentData(data, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            memoryDetailsViewModel.coverPhotoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCoverPhotoChanged()
            memoryDetailsViewModel.doneImportingCoverImageFromGallery()
            memoryDetailsViewModel.onUpdateMemoryCoverPhoto()
        }

        if(resultCode == Activity.RESULT_OK && requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            val srcFile = getRealPathFromUri(imageUri, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            memoryDetailsViewModel.photoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCreateMemoryPhoto()
            memoryDetailsViewModel.doneImportingImageFromCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    addPhotoFromCamera(8880)
                } else {
                    Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onFinishEditDialog(inputText: String) {
        memoryDetailsViewModel.memoryDescription.value = inputText
        memoryDetailsViewModel.onUpdateMemoryDescription()
        memoryDetailsViewModel.doneShowingDescriptionDialogFragment()
    }
}