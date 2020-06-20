package com.example.traveljournal.journeyDetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.example.traveljournal.*
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneyDetailsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class JourneyDetailsFragment: Fragment() {
    private lateinit var journeyDetailsViewModel: JourneyDetailsViewModel
    private lateinit var backupPhotoPath: String
    private lateinit var binding: FragmentJourneyDetailsBinding

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
            inflater, R.layout.fragment_journey_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = JourneyDetailsFragmentArgs.fromBundle(requireArguments())

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = JourneyDetailsViewModelFactory(arguments.journeyKey, dataSource)

        journeyDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(JourneyDetailsViewModel::class.java)

        binding.journeyDetailsViewModel = journeyDetailsViewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        val adapter = ExperienceAdapter()
        binding.experiencesList.adapter = adapter

        backupPhotoPath = getBackupPath(requireContext()) + "Media/"

        journeyDetailsViewModel.navigateToJourneys.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToJourneysDestination())
                journeyDetailsViewModel.doneNavigatingToJourneys()
            }
        })

        journeyDetailsViewModel.experiences.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                if(it.isNotEmpty()) {
                    binding.emptyExperiencesListImage.visibility = ConstraintLayout.GONE
                } else {
                    binding.emptyExperiencesListImage.visibility = ConstraintLayout.VISIBLE
                }
            }
        })

        journeyDetailsViewModel.navigateToNewExperience.observe(viewLifecycleOwner, Observer { journeyKey ->
            journeyKey?.let {
                this.findNavController().navigate(
                    JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToNewExperienceDestination(journeyKey))
                journeyDetailsViewModel.doneNavigatingToNewExperience()
            }
        })

        journeyDetailsViewModel.showSnackbarEventExperiencesDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_experiences_message,
                        journeyDetailsViewModel.getJourney().value?.placeName),
                        Snackbar.LENGTH_SHORT
                    ).show()
                journeyDetailsViewModel.doneShowingSnackbarExperiencesDeleted()
            }
        })

        journeyDetailsViewModel.showSnackbarEventJourneyDeleted.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.journey_deleted,
                        journeyDetailsViewModel.getJourney().value?.placeName),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeyDetailsViewModel.doneShowingSnackbarJourneyDeleted()
            }
        })

        journeyDetailsViewModel.initiateImageImportFromGallery.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                requestStoragePermission()
            }
        })

        journeyDetailsViewModel.openCoverPhotoDialogFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val dialogFragment = JourneyCoverDialogFragment(journeyDetailsViewModel)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        waitForTransition(binding.experiencesList)
        waitForTransition(binding.journeyImage)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journey_details_overflow_menu, menu)

        val deleteAllExperiencesMenu = menu.findItem(R.id.delete_all_experiences_menu)
        val spannabledeleteAllExperiencesMenuString = SpannableString(deleteAllExperiencesMenu.title.toString())
        spannabledeleteAllExperiencesMenuString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannabledeleteAllExperiencesMenuString.length, 0)
        deleteAllExperiencesMenu.title = spannabledeleteAllExperiencesMenuString

        val deleteJourneyMenu = menu.findItem(R.id.delete_journey_menu)
        val spannabledeleteJourneyMenuString = SpannableString(deleteJourneyMenu.title.toString())
        spannabledeleteJourneyMenuString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.errorColor)), 0, spannabledeleteJourneyMenuString.length, 0)
        deleteJourneyMenu.title = spannabledeleteJourneyMenuString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.change_journey_cover_photo_menu) {
            journeyDetailsViewModel.onChangeCoverPhotoClicked()
            journeyDetailsViewModel.doneImportingImageFromGallery()
        }

        if(id == R.id.delete_all_experiences_menu) {
            val dialogFragment = DeleteAllExperiencesDialogFragment(journeyDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_all_experiences_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_all_experiences_dialog")

            return true
        }

        if(id == R.id.delete_journey_menu) {
            val dialogFragment = DeleteJourneyDialogFragment(journeyDetailsViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("delete_journey_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "delete_journey_dialog")

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(
                    requireContext(),
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.type = "image/*"
        startActivityForResult(intent, 9000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9000) {
            val srcFile = getRealPathForIntentData(data, requireContext())
            val destFile = File(backupPhotoPath, srcFile.name)
            backupPhoto(srcFile, destFile, backupPhotoPath)

            journeyDetailsViewModel.coverPhotoSrcUri.value = destFile.toString()
            journeyDetailsViewModel.onCoverPhotoChanged()
            journeyDetailsViewModel.doneImportingImageFromGallery()
            journeyDetailsViewModel.onUpdateJourney()
        }
    }
}