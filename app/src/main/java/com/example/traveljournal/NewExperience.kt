package com.example.traveljournal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.traveljournal.databinding.FragmentNewExperienceBinding

/**
 * A simple [Fragment] subclass.
 */
class NewExperience : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.createExperience)
        val binding: FragmentNewExperienceBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_new_experience, container, false
        )
        return binding.root
    }


}
