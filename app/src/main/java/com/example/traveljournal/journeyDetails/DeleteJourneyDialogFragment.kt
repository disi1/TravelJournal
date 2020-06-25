package com.example.traveljournal.journeyDetails

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogDeleteJourneyBinding

class DeleteJourneyDialogFragment(val journeyDetailsViewModel: JourneyDetailsViewModel): DialogFragment() {

    private lateinit var deleteButton: Button
    private lateinit var cancelButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogDeleteJourneyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_delete_journey,
            container,
            false
        )

        val deleteJourneyQuestionFirstPart = getString(R.string.delete_journey_question)
        val journeyName = journeyDetailsViewModel.getJourney().value?.placeName

        val deleteJourneyQuestion = "$deleteJourneyQuestionFirstPart <b>$journeyName</b>?"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.deleteJourneyQuestion.text = Html.fromHtml(deleteJourneyQuestion, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            binding.deleteJourneyQuestion.text = Html.fromHtml(deleteJourneyQuestion)
        }
        
        deleteButton = binding.deleteButton
        cancelButton = binding.cancelButton

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteButton.setOnClickListener {
            journeyDetailsViewModel.onDeleteJourney()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        val display: Display = window!!.windowManager.defaultDisplay

        display.getSize(size)
        window.setLayout((size.x * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setLayout((size.y * 0.50).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)

        super.onResume()
    }
}