package com.example.traveljournal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.traveljournal.databinding.FragmentExperienceBinding

/**
 * A simple [Fragment] subclass.
 */
class Experience : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentExperienceBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_experience, container, false
        )
        return binding.root
    }


}
