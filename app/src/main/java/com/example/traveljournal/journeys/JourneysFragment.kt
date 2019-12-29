package com.example.traveljournal.journeys


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentJourneysBinding

class JourneysFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.myJourneys)
        val binding: FragmentJourneysBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journeys, container, false
        )
        binding.newJourneyButton.setOnClickListener (
            Navigation.createNavigateOnClickListener(R.id.action_journeys_destination_to_new_journey_destination)
        )
        return binding.root
    }
}
