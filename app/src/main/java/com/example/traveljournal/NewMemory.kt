package com.example.traveljournal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.traveljournal.databinding.FragmentNewMemoryBinding

/**
 * A simple [Fragment] subclass.
 */
class NewMemory : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentNewMemoryBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_new_memory, container, false
        )
        return binding.root
    }


}
