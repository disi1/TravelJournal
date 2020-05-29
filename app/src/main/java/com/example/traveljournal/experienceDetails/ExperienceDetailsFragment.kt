package com.example.traveljournal.experienceDetails


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traveljournal.R
import com.example.traveljournal.backupPhoto
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentExperienceDetailsBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getRealPath
import com.google.android.material.snackbar.Snackbar
import java.io.File
import kotlin.math.exp


class ExperienceDetailsFragment : Fragment(), ExperienceDescriptionDialogFragment.DialogListener {

    private lateinit var experienceDetailsViewModel: ExperienceDetailsViewModel
    private lateinit var adapter: MemoryAdapter
    private lateinit var backupPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.experience_details)

        val binding: FragmentExperienceDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_experience_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = ExperienceDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = ExperienceDetailsViewModelFactory(arguments.experienceKey, dataSource)

        experienceDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ExperienceDetailsViewModel::class.java)

        binding.experienceDetailsViewModel = experienceDetailsViewModel
        binding.lifecycleOwner = this

        backupPhotoPath = getBackupPath(context!!) + "Media/"

        val manager = LinearLayoutManager(activity)
        binding.memoriesList.layoutManager = manager
        adapter = MemoryAdapter(MemoryListener {
            memoryId -> experienceDetailsViewModel.onMemoryClicked(memoryId)
        }, experienceDetailsViewModel)
        binding.memoriesList.adapter = adapter

        experienceDetailsViewModel.memories.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })

        experienceDetailsViewModel.experienceDescription.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.notifyItemChanged(0, null)
            }
        })

        experienceDetailsViewModel.showSnackbarEventMemoriesDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_memories_message,
                        experienceDetailsViewModel.getExperience().value?.experienceName),
                    Snackbar.LENGTH_SHORT
                ).show()
                experienceDetailsViewModel.doneShowingSnackbarMemoriesDeleted()
            }
        })

        experienceDetailsViewModel.showSnackbarEventExperienceDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.experience_deleted,
                        experienceDetailsViewModel.getExperience().value?.experienceName),
                    Snackbar.LENGTH_SHORT
                ).show()
                experienceDetailsViewModel.doneShowingSnackbarExperienceDeleted()
            }
        })

        experienceDetailsViewModel.navigateToJourneyDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { journeyKey ->
            journeyKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToJourneyDetailsDestination(journeyKey))
                experienceDetailsViewModel.doneNavigatingToJourneyDetails()
            }
        })

        experienceDetailsViewModel.navigateToNewMemory.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToNewMemoryDestination(experienceKey))
                experienceDetailsViewModel.doneNavigatingToNewMemory()
            }
        })

        experienceDetailsViewModel.navigateToMemoryDetails.observe(viewLifecycleOwner, Observer { memoryKey ->
            memoryKey?.let {
                this.findNavController().navigate(
                    ExperienceDetailsFragmentDirections.actionExperienceDetailsDestinationToMemoryDetailsFragment(memoryKey))
                experienceDetailsViewModel.doneNavigatingToMemoryDetails()
            }
        })

        experienceDetailsViewModel.openDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = ExperienceDescriptionDialogFragment(experienceDetailsViewModel)

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

        experienceDetailsViewModel.initiateImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestStoragePermission()
            }
        })

        experienceDetailsViewModel.openCoverPhotoDialogFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val dialogFragment = ExperienceCoverDialogFragment(experienceDetailsViewModel)

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

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.experience_details_overflow_menu, menu)

        val deleteAllMemoriesMenu = menu.findItem(R.id.delete_all_memories_menu)
        val spannableDeleteAllMemoriesMenuString = SpannableString(deleteAllMemoriesMenu.title.toString())
        spannableDeleteAllMemoriesMenuString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, android.R.color.holo_red_light)), 0, spannableDeleteAllMemoriesMenuString.length, 0)
        deleteAllMemoriesMenu.title = spannableDeleteAllMemoriesMenuString

        val deleteExperienceMenu = menu.findItem(R.id.delete_experience_menu)
        val spannableDeleteExperienceMenuString = SpannableString(deleteExperienceMenu.title.toString())
        spannableDeleteExperienceMenuString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, android.R.color.holo_red_light)), 0, spannableDeleteExperienceMenuString.length, 0)
        deleteExperienceMenu.title = spannableDeleteExperienceMenuString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_memories_menu) {
            val dialogFragment = DeleteAllMemoriesDialogFragment(experienceDetailsViewModel)

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

        if(id == R.id.delete_experience_menu) {
            val dialogFragment = DeleteExperienceDialogFragment(experienceDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_experience_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_experience_dialog")

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                requestPermissions(permission, 9999)
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, 9999)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9999) {
            val srcFile = getRealPath(data, context!!)
            val destFile = File(backupPhotoPath, srcFile.name)


            backupPhoto(srcFile, destFile, backupPhotoPath)
            experienceDetailsViewModel.coverPhotoSrcUri.value = destFile.toString()
            experienceDetailsViewModel.onCoverPhotoChanged()
            experienceDetailsViewModel.doneImportingImageFromGallery()
            experienceDetailsViewModel.onUpdateExperienceCoverPhoto()
        }
    }

    override fun onFinishEditDialog(inputText: String) {
        experienceDetailsViewModel.experienceDescription.value = inputText
        experienceDetailsViewModel.onUpdateExperienceDescription()
        experienceDetailsViewModel.doneShowingDialogFragment()
    }
}
