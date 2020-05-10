package com.example.traveljournal.memoryDetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentMemoryDetailsBinding
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
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.memory_details)

        val binding : FragmentMemoryDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_memory_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = MemoryDetailsFragmentArgs.fromBundle(arguments!!)

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

        backupPhotoPath = context!!.getExternalFilesDir(null)!!.path + "/Backup/" + "Media/"

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
                requestStoragePermission()
            }
        })

        memoryDetailsViewModel.showSnackbarEventMemoryPhotosDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_memory_photos_message,
                        memoryDetailsViewModel.getMemory().value?.memoryName),
                    Snackbar.LENGTH_SHORT
                ).show()
                memoryDetailsViewModel.doneShowingSnackbarMemoryPhotosDeleted()
            }
        })

        memoryDetailsViewModel.openDialogFragment.observe(viewLifecycleOwner, Observer {
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.clear_all_memory_photos_menu) {
            memoryDetailsViewModel.onClear()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                requestPermissions(permission, 9990) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, 9990)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9990) {
            val srcFile = getRealPath(data)
            val destFile = File(backupPhotoPath, srcFile.name)

            memoryDetailsViewModel.backupPhoto(srcFile, destFile, backupPhotoPath)
            memoryDetailsViewModel.photoSrcUri.value = destFile.toString()
            memoryDetailsViewModel.onCreateMemoryPhoto()
        }
    }

    private fun getRealPath(data: Intent?): File {
        val selectedImage = data?.data
        val cursor = context!!.contentResolver.query(
            selectedImage!!,
            arrayOf(MediaStore.Images.ImageColumns.DATA),
            null,
            null,
            null
        )
        cursor!!.moveToFirst()

        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val selectedImagePath = cursor.getString(idx)
        cursor.close()

        return File(selectedImagePath)
    }

    override fun onFinishEditDialog(inputText: String) {
        memoryDetailsViewModel.memoryDescription.value = inputText
        memoryDetailsViewModel.onUpdateMemory()
        memoryDetailsViewModel.doneShowingDialogFragment()
    }
}