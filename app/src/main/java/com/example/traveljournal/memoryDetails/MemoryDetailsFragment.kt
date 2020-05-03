package com.example.traveljournal.memoryDetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentMemoryDetailsBinding
import com.google.android.material.snackbar.Snackbar

class MemoryDetailsFragment: Fragment() {
    private lateinit var memoryDetailsViewModel: MemoryDetailsViewModel

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
        intent.type = "image/*"
        startActivityForResult(intent, 9990)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9990) {
            memoryDetailsViewModel.photoSrcUri.value = data?.data.toString()
            memoryDetailsViewModel.onCreateMemoryPhoto()
        }
    }
}