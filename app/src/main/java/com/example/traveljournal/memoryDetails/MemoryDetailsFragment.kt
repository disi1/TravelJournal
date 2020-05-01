package com.example.traveljournal.memoryDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentMemoryDetailsBinding

class MemoryDetailsFragment: Fragment() {
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

        val memoryDetailsViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(MemoryDetailsViewModel::class.java)

        binding.memoryDetailsViewModel = memoryDetailsViewModel

        binding.lifecycleOwner = this

        return binding.root
    }
}