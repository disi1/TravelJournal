package com.example.traveljournal.memory

import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.R
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentNewMemoryBinding
import java.text.DateFormat
import java.util.*

class NewMemoryFragment: Fragment() {
    private lateinit var newMemoryViewModel: NewMemoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.create_memory)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.backgroundColor))
        )

        val binding: FragmentNewMemoryBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_memory,
            container,
            false)

        val application = requireNotNull(this.activity).application

        val arguments = NewMemoryFragmentArgs.fromBundle(requireArguments())

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = NewMemoryViewModelFactory(arguments.experienceKey, dataSource)

        newMemoryViewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(NewMemoryViewModel::class.java)

        binding.newMemoryViewModel = newMemoryViewModel

        val currentTime = Calendar.getInstance().time
        binding.memoryDate.text = DateFormat.getDateInstance(DateFormat.LONG).format(currentTime)
        newMemoryViewModel.memoryTimestamp.value = currentTime.time

        binding.memoryNameInputText.afterTextChanged { memoryName ->
            newMemoryViewModel.memoryName.value = memoryName
            binding.createButton.isEnabled = true
        }

        binding.memoryDescriptionInputText.afterTextChanged { memoryDescription ->
            newMemoryViewModel.memoryDescription.value = memoryDescription
        }

        newMemoryViewModel.chooseDateTextViewClickedClicked.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                showDatePickerDialog(binding.memoryDate)
            }
        })

        newMemoryViewModel.navigateToExperienceDetails.observe(viewLifecycleOwner, Observer { experienceKey ->
            experienceKey?.let {
                this.findNavController().navigate(
                    NewMemoryFragmentDirections.actionNewMemoryDestinationToExperienceDetailsDestination(experienceKey))

                newMemoryViewModel.doneNavigatingToExperienceDetails()
            }
        })

        return binding.root
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showDatePickerDialog(memoryDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, thisYear, thisMonth, thisDay ->
            calendar[thisYear, thisMonth] = thisDay

            memoryDateTextView.text = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.time)

            newMemoryViewModel.memoryTimestamp.value = calendar.time.time
        }, year, month, day)
        datePickerDialog.show()
    }
}