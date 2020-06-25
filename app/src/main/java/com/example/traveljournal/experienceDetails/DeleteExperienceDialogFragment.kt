package com.example.traveljournal.experienceDetails

import android.graphics.Point
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogDeleteExperienceBinding


class DeleteExperienceDialogFragment(val experienceDetailsViewModel: ExperienceDetailsViewModel): DialogFragment() {

    private lateinit var deleteButton: Button
    private lateinit var cancelButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogDeleteExperienceBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_delete_experience,
            container,
            false
        )

        val deleteQuestionFirstPart = getString(R.string.delete_question_first_part)
        val experienceName = experienceDetailsViewModel.getExperience().value?.experienceName

        val deleteExperienceQuestion = "$deleteQuestionFirstPart <b>$experienceName</b>?"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.deleteExperienceQuestion.text = Html.fromHtml(deleteExperienceQuestion, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            binding.deleteExperienceQuestion.text = Html.fromHtml(deleteExperienceQuestion)
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
            experienceDetailsViewModel.onDeleteExperience()
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