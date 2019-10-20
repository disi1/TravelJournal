package com.example.traveljournal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.traveljournal.databinding.FragmentJourneyBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass.
 */
class Journey : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentJourneyBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journey, container, false
        )

        return binding.root
    }
}
