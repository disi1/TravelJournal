package com.example.traveljournal.journeys

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

private const val DATABASE_NAME = "travel_history_database"

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val journeys = database.getAllJourneys()

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    private var _showSnackBarEventJourneyDeleted = MutableLiveData<Boolean>()
    val showSnackBarEventJourneyDeleted: LiveData<Boolean>
        get() = _showSnackBarEventJourneyDeleted

    private var _showSnackBarEventDataBackedUp = MutableLiveData<Boolean>()
    val showSnackBarEventDataBackedUp: LiveData<Boolean>
        get() = _showSnackBarEventDataBackedUp

    private var _showSnackBarEventDataRestored = MutableLiveData<Boolean>()
    val showSnackBarEventDataRestored: LiveData<Boolean>
        get() = _showSnackBarEventDataRestored

    private val _navigateToNewJourney = MutableLiveData<Boolean?>()
    val navigateToNewJourney: LiveData<Boolean?>
        get() = _navigateToNewJourney

    private val _navigateToJourneyDetails = MutableLiveData<Long>()
    val navigateToJourneyDetails: LiveData<Long>
        get() = _navigateToJourneyDetails

    private val _launchBackupMechanism = MutableLiveData<Boolean?>()
    val launchBackupMechanism: LiveData<Boolean?>
        get() = _launchBackupMechanism

    private val _launchRestoreMechanism = MutableLiveData<Boolean?>()
    val launchRestoreMechanism: LiveData<Boolean?>
        get() = _launchRestoreMechanism

    private val _openBackupDialogFragment = MutableLiveData<Boolean?>()
    val openBackupDialogFragment: LiveData<Boolean?>
        get() = _openBackupDialogFragment

    private val _openRestoreDialogFragment = MutableLiveData<Boolean?>()
    val openRestoreDialogFragment: LiveData<Boolean?>
        get() = _openRestoreDialogFragment

    fun doneShowingBackupDialogFragment() {
        _openBackupDialogFragment.value = false
    }

    fun doneShowingRestoreDialogFragment() {
        _openRestoreDialogFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun doneShowingSnackbarJourneyDeleted() {
        _showSnackBarEventJourneyDeleted.value = false
    }

    fun doneShowingSnackbarDataBackedUp() {
        _showSnackBarEventDataBackedUp.value = false
    }

    fun doneShowingSnackbarDataRestored() {
        _showSnackBarEventDataRestored.value = false
    }

    fun onBackupMechanismDone() {
        _launchBackupMechanism.value = false
    }

    fun onRestoreMechanismDone() {
        _launchRestoreMechanism.value = false
    }

    fun doneNavigating() {
        _navigateToNewJourney.value = null
    }

    fun onNewJourney() {
        _navigateToNewJourney.value = true
    }

    fun onClear() {
        uiScope.launch {
            clear()
            _showSnackbarEvent.value = true
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clearPhotos()
            database.clearMemories()
            database.clearExperiences()
            database.clearJourneys()
        }
    }

    fun onDeleteJourney(journey: Journey) {
        uiScope.launch {
            deleteJourney(journey)

            _showSnackBarEventJourneyDeleted.value = true
        }
    }

    private suspend fun deleteJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            database.deleteJourney(journey)
        }
    }

    fun onJourneyClicked(id: Long) {
        _navigateToJourneyDetails.value = id
    }

    fun onJourneyDetailsNavigated() {
        _navigateToJourneyDetails.value = null
    }

    fun onBackupButtonClicked() {
        _openBackupDialogFragment.value = true
    }

    fun onDialogBackupButtonClicked() {
        _launchBackupMechanism.value = true
    }

    fun onRestoreButtonClicked() {
        _openRestoreDialogFragment.value = true
    }

    fun onDialogRestoreButtonClicked() {
        _launchRestoreMechanism.value = true
    }

    fun backup(context: Context, backupPath: String) {
        val dbFilePath = context.getDatabasePath(DATABASE_NAME).path
        val dbShmFilePath = context.getDatabasePath("$DATABASE_NAME-shm").path
        val dbWalFilePath = context.getDatabasePath("$DATABASE_NAME-wal").path

        val backupDbDir = backupPath
        val file = File(backupDbDir)
        if (!file.exists()) {
            file.mkdirs()
        }

        val dbFileExternalPath = backupDbDir + DATABASE_NAME
        val dbShmFileExternalPath = backupDbDir + DATABASE_NAME + "-shm"
        val dbWalFileExternalPath = backupDbDir + DATABASE_NAME + "-wal"

        try {
            copyFromTo(dbFilePath, dbFileExternalPath)
            copyFromTo(dbShmFilePath, dbShmFileExternalPath)
            copyFromTo(dbWalFilePath, dbWalFileExternalPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _showSnackBarEventDataBackedUp.value = true
    }

    fun restore(context: Context, backupPath: String) {
        val dbFileExternalPath = backupPath + DATABASE_NAME
        val dbShmFileExternalPath = backupPath + DATABASE_NAME + "-shm"
        val dbWalFileExternalPath = backupPath + DATABASE_NAME + "-wal"

        val dbFilePath = context.getDatabasePath(DATABASE_NAME).path
        val dbShmFilePath = context.getDatabasePath("$DATABASE_NAME-shm").path
        val dbWalFilePath = context.getDatabasePath("$DATABASE_NAME-wal").path

        try {
            copyFromTo(dbFileExternalPath, dbFilePath)
            copyFromTo(dbShmFileExternalPath, dbShmFilePath)
            copyFromTo(dbWalFileExternalPath, dbWalFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _showSnackBarEventDataRestored.value = true
    }

    private fun copyFromTo(fromPath: String, toPath: String) {
        val inStream = File(fromPath).inputStream()
        val outStream = FileOutputStream(toPath)
        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}