package com.example.traveljournal.journey

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentNewJourneyBinding
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


class NewJourneyFragment : Fragment(), PlaceSelectionListener {

    private lateinit var bitmapCover: Bitmap
    private lateinit var attributions: String
    private lateinit var newJourneyViewModel: NewJourneyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.create_journey)

        val binding: FragmentNewJourneyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_journey, container, false
        )

        val application = requireNotNull(this.activity).application

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = NewJourneyViewModelFactory(dataSource)

        newJourneyViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(NewJourneyViewModel::class.java)

        binding.newJourneyViewModel = newJourneyViewModel

        newJourneyViewModel.navigateToJourneys.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    NewJourneyFragmentDirections.actionNewJourneyDestinationToJourneysDestination())
                newJourneyViewModel.doneNavigating()
            }
        })

        newJourneyViewModel.bitmapCoverLoaded.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                binding.creditsText.text = HtmlCompat.fromHtml(getString(R.string.credits_to, attributions), HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.creditsText.movementMethod = LinkMovementMethod.getInstance()
                binding.journeyImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.journeyImage.setImageBitmap(bitmapCover)
            }
        })

        if (!Places.isInitialized()) {
            this.context?.let { Places.initialize(it, getString(R.string.apiKey), Locale.US) }
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as? AutocompleteSupportFragment
        autocompleteFragment?.setOnPlaceSelectedListener(this)
        autocompleteFragment!!.setHint(getString(R.string.where_question))
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS))

        val clearButton = autocompleteFragment.view?.findViewById<View>(R.id.places_autocomplete_clear_button)
        clearButton?.setOnClickListener {
            autocompleteFragment.setText("")
            newJourneyViewModel.coverPhotoAttributions.value = null
            newJourneyViewModel.coverPhotoSrcUri.value = null
            newJourneyViewModel.selectedPlaceName.value = null
            newJourneyViewModel.selectedPlaceAddress.value = null
            binding.creditsText.text = ""
            binding.journeyImage.setImageResource(R.drawable.ic_undraw_destinations)
        }

        return binding.root
    }

    @ExperimentalStdlibApi
    override fun onPlaceSelected(p0: Place) {

        val photoMetadata = p0.photoMetadatas?.get(0)
        attributions = photoMetadata?.attributions.toString()

        val photoRequest = photoMetadata?.let { FetchPhotoRequest.builder(it).build() }

        val placesClient = Places.createClient(this.context!!)

        if (photoRequest != null) {
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener {

                val backupPhotoPath = getBackupPath(context!!) + "Media/"

                bitmapCover = it.bitmap
                newJourneyViewModel.onBitmapCoverLoaded()
                val savedBitmapPath = saveBitmap(bitmapCover, "${System.currentTimeMillis() / 1000L}.png", backupPhotoPath)
                newJourneyViewModel.coverPhotoSrcUri.value = savedBitmapPath
                newJourneyViewModel.coverPhotoAttributions.value = attributions

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e("ERROR", "$statusCode - Place not found: " + exception.message)
                }
            }
        }

        newJourneyViewModel.selectedPlaceName.value = p0.name
        newJourneyViewModel.selectedPlaceAddress.value = p0.address
    }

    override fun onError(status: Status) {
        Log.e("ERROR", "An error occurred: $status")
    }
}
