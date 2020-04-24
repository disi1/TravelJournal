package com.example.traveljournal.experienceDetails


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentExperienceDetailsBinding

class ExperienceDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.experience)

        val binding: FragmentExperienceDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_experience_details, container, false
        )

        val application = requireNotNull(this.activity).application
        val arguments = ExperienceDetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = ExperienceDetailsViewModelFactory(arguments.experienceKey, dataSource)

        val experienceDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(ExperienceDetailsViewModel::class.java)

        binding.experienceDetailsViewModel = experienceDetailsViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
