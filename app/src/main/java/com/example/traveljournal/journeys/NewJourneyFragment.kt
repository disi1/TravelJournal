package com.example.traveljournal.journeys

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentNewJourneyBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*
import kotlin.reflect.typeOf

class   NewJourneyFragment : Fragment(), PlaceSelectionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.createJourney)

        val binding: FragmentNewJourneyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_journey, container, false
        )
        setHasOptionsMenu(true)

        if (!Places.isInitialized()) {
            this.context?.let { Places.initialize(it, getString(R.string.apiKey), Locale.US) };
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as? AutocompleteSupportFragment
        autocompleteFragment?.setOnPlaceSelectedListener(this)
        autocompleteFragment!!.setHint(getString(R.string.destinationExample));
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.create_journey_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.createJourneyButton) {
            Toast.makeText(this.context, "Item Clicked", Toast.LENGTH_LONG).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @ExperimentalStdlibApi
    override fun onPlaceSelected(p0: Place) {
        Log.i("PLACE", "Place: " + p0.getName() + ", " + p0.getId());
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context,""+status.toString(),Toast.LENGTH_LONG).show();
        Log.i("ERROR", "An error occurred: " + status);
    }
}
