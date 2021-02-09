package com.example.traveljournal.journeys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.MainActivity
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogRestoreBinding
import com.example.traveljournal.getBackupPath
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RestoreDialogFragment(val journeysViewModel: JourneysViewModel) : DialogFragment(),
    CoroutineScope {
    private lateinit var cancelButton: TextView
    private lateinit var restoreButton: Button
    private lateinit var progressBar: ProgressBar

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDialogRestoreBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_restore,
            container,
            false
        )

        binding.journeysViewModel = journeysViewModel

        cancelButton = binding.cancelButton
        restoreButton = binding.restoreButton
        progressBar = binding.indeterminateBar

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            restoreButton.isEnabled = false
            cancelButton.isEnabled = false

            launch {
                withContext(Dispatchers.IO) {
                    journeysViewModel.onRestore(requireContext(), getBackupPath(requireContext()))
                }

                journeysViewModel.doneShowingRestoreDialogFragment()
                dismiss()

                triggerRestart(requireContext())
            }
        }

        cancelButton.setOnClickListener {
            journeysViewModel.doneShowingRestoreDialogFragment()
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

    private fun triggerRestart(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Runtime.getRuntime().exit(0)
    }
}