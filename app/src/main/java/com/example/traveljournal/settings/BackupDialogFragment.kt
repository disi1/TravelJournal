package com.example.traveljournal.settings

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.traveljournal.R
import com.example.traveljournal.databinding.FragmentDialogBackupBinding
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.getCurrentDateAndTime
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class BackupDialogFragment(private val settingsViewModel: SettingsViewModel) : DialogFragment(),
    CoroutineScope {

    private lateinit var backUpButton: Button
    private lateinit var cancelButton: TextView
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
        val binding: FragmentDialogBackupBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dialog_backup,
            container,
            false
        )

        backUpButton = binding.backupButton
        cancelButton = binding.cancelButton
        progressBar = binding.indeterminateBar

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backUpButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            backUpButton.isEnabled = false
            cancelButton.isEnabled = false

            val zipBackupFile = getBackupPath(requireContext()) + "TravelDiaryBackup.bdia"

            launch {
                withContext(Dispatchers.IO) {
                    settingsViewModel.onExternalStorageBackup(
                        getBackupPath(requireContext()),
                        zipBackupFile,
                        requireContext()
                    )
                }

                backUpDataExternally(zipBackupFile, requireContext())

                settingsViewModel.doneShowingBackupDialogFragment()
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            settingsViewModel.doneShowingBackupDialogFragment()
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

    private fun backUpDataExternally(backupZipFilePath: String, context: Context) {
        val backupZipFile = File(backupZipFilePath)
        val contentUri: Uri =
            getUriForFile(context, "com.example.traveljournal.fileprovider", backupZipFile)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Travel Diary Backup")
            putExtra(Intent.EXTRA_TEXT, "You backed up your data at ${getCurrentDateAndTime()}.")
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/bdia"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}