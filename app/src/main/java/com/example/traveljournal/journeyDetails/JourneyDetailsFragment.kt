package com.example.traveljournal.journeyDetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.backupPhoto
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneyDetailsBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getRealPath
import com.google.android.material.snackbar.Snackbar
import java.io.File

class JourneyDetailsFragment: Fragment() {
    private lateinit var journeyDetailsViewModel: JourneyDetailsViewModel
    private lateinit var backupPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.journey_details)

        val binding: FragmentJourneyDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journey_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = JourneyDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = JourneyDetailsViewModelFactory(arguments.journeyKey, dataSource)

        journeyDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(JourneyDetailsViewModel::class.java)

        binding.journeyDetailsViewModel = journeyDetailsViewModel
        binding.lifecycleOwner = this

        val adapter = ExperienceAdapter(ExperienceListener {
            experienceId -> journeyDetailsViewModel.onExperienceClicked(experienceId)
        })
        binding.experiencesList.adapter = adapter

        backupPhotoPath = getBackupPath(context!!) + "Media/"

        journeyDetailsViewModel.experiences.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        journeyDetailsViewModel.navigateToNewExperience.observe(viewLifecycleOwner, Observer { journeyKey ->
            journeyKey?.let {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToNewExperienceDestination(journeyKey))
                journeyDetailsViewModel.doneNavigatingToNewExperience()
            }
        })

        journeyDetailsViewModel.navigateToExperienceDetails.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToExperienceDetailsDestination(experienceKey))
                journeyDetailsViewModel.doneNavigatingToExperienceDetails()
            }
        })

        journeyDetailsViewModel.showSnackbarEventExperiencesDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_experiences_message,
                        journeyDetailsViewModel.getJourney().value?.placeName),
                        Snackbar.LENGTH_SHORT
                    ).show()
                journeyDetailsViewModel.doneShowingSnackbarExperiencesDeleted()
            }
        })

        journeyDetailsViewModel.initiateImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestStoragePermission()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journey_details_overflow_menu, menu)

        val deleteAllExperiencesMenu = menu.findItem(R.id.delete_all_experiences_menu)
        val spannableString = SpannableString(deleteAllExperiencesMenu.title.toString())
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.colorAccent)), 0, spannableString.length, 0)
        deleteAllExperiencesMenu.title = spannableString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_experiences_menu) {
            val dialogFragment = DeleteAllExperiencesDialogFragment(journeyDetailsViewModel)

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

        if(id == R.id.change_journey_cover_photo_menu) {
            journeyDetailsViewModel.onChangeCoverPhotoClicked()
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

                requestPermissions(permission, 9000)
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, 9000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9000) {
            val srcFile = getRealPath(data, context!!)
            val destFile = File(backupPhotoPath, srcFile.name)


            backupPhoto(srcFile, destFile, backupPhotoPath)
            journeyDetailsViewModel.coverPhotoSrcUri.value = destFile.toString()
            journeyDetailsViewModel.onCoverPhotoChanged()
            journeyDetailsViewModel.doneImportingImageFromGallery()
            journeyDetailsViewModel.onUpdateJourney()
        }
    }
}