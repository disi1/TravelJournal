package com.example.traveljournal.experience

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.BuildConfig
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentNewExperienceBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.saveBitmap
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

class NewExperienceFragment : Fragment(), PlaceSelectionListener {

    private lateinit var bitmapCover: Bitmap
    private lateinit var attributions: String
    private lateinit var newExperienceViewModel: NewExperienceViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.create_experience)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.backgroundColor)
            )
        )

        val binding: FragmentNewExperienceBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_experience,
            container,
            false
        )

        val application = requireNotNull(this.activity).application

        val arguments = NewExperienceFragmentArgs.fromBundle(requireArguments())

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = NewExperienceViewModelFactory(arguments.journeyKey, dataSource)

        newExperienceViewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(NewExperienceViewModel::class.java)

        binding.newExperienceViewModel = newExperienceViewModel

        progressBar = binding.indeterminateBar

        newExperienceViewModel.navigateToJourneyDetails.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { journeyKey ->
                journeyKey?.let {
                    this.findNavController().navigate(
                        NewExperienceFragmentDirections.actionNewExperienceDestinationToJourneyDetailsDestination(
                            journeyKey
                        )
                    )
                    newExperienceViewModel.doneNavigatingToJourneyDetails()
                }
            })

        newExperienceViewModel.bitmapCoverLoaded.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.creditsText.text = HtmlCompat.fromHtml(
                    getString(R.string.credits_to, attributions),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                binding.creditsText.movementMethod = LinkMovementMethod.getInstance()
                binding.experienceImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.experienceImage.setImageBitmap(bitmapCover)
                progressBar.visibility = View.GONE
                binding.createButton.isEnabled = true
            } else if (it == false) {
                binding.createButton.isEnabled = true
            }
        })

        if (!Places.isInitialized()) {
            this.context?.let { Places.initialize(it, BuildConfig.API_KEY, Locale.US) }
        }

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.experience_autocomplete_fragment)
                    as? AutocompleteSupportFragment
        autocompleteFragment?.setOnPlaceSelectedListener(this)
        autocompleteFragment!!.setHint(getString(R.string.where_to))
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.PHOTO_METADATAS
            )
        )
        (autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_search_input) as EditText).setTextColor(
            Color.WHITE
        )
        (autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_search_input) as EditText).textSize =
            18f
        (autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_search_input) as EditText).setHintTextColor(
            Color.GRAY
        )

        val clearButton =
            autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
        clearButton?.setOnClickListener {
            autocompleteFragment.setText("")
            newExperienceViewModel.selectedExperiencePlaceName.value = null
            newExperienceViewModel.selectedExperiencePlaceAddress.value = null
            newExperienceViewModel.coverPhotoAttributions.value = null
            newExperienceViewModel.coverPhotoSrcUri.value = null
            binding.creditsText.text = ""
            binding.experienceImage.setImageResource(R.drawable.ic_undraw_experience)
            binding.createButton.isEnabled = false
            progressBar.visibility = View.GONE
        }

        binding.experienceNameEditText.afterTextChanged { experienceName ->
            newExperienceViewModel.experienceName.value = experienceName
            binding.createButton.isEnabled = experienceName != ""
        }

        binding.experienceDescriptionEditText.afterTextChanged { experienceDescription ->
            newExperienceViewModel.experienceDescription.value = experienceDescription
        }

        return binding.root
    }

    override fun onPlaceSelected(p0: Place) {
        if (p0.photoMetadatas != null) {
            progressBar.visibility = View.VISIBLE

            val photoMetadata = p0.photoMetadatas?.get(0)
            attributions = photoMetadata?.attributions.toString()

            val photoRequest = photoMetadata?.let { FetchPhotoRequest.builder(it).build() }

            val placesClient = Places.createClient(this.requireContext())

            if (photoRequest != null) {
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener {

                    val backupPhotoPath = getBackupPath(requireContext()) + "Media/"

                    bitmapCover = it.bitmap
                    newExperienceViewModel.onBitmapCoverLoaded(true)
                    val savedBitmapPath = saveBitmap(
                        bitmapCover,
                        "${System.currentTimeMillis() / 1000L}.png",
                        backupPhotoPath
                    )
                    newExperienceViewModel.coverPhotoSrcUri.value = savedBitmapPath
                    newExperienceViewModel.coverPhotoAttributions.value = attributions

                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Log.e("ERROR", "$statusCode - Place not found: " + exception.message)
                    }
                }
            }
        } else {
            newExperienceViewModel.onBitmapCoverLoaded(false)
        }

        newExperienceViewModel.selectedExperiencePlaceName.value = p0.name
        newExperienceViewModel.selectedExperiencePlaceAddress.value = p0.address
    }

    override fun onError(p0: Status) {
        Log.i("ERROR", "An error occurred: " + p0)
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}