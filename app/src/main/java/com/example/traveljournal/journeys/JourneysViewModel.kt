package com.example.traveljournal.journeys

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.traveljournal.database.Journey
import com.example.traveljournal.database.TravelDatabaseDao
import com.example.traveljournal.getBackupPath
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    private val _launchLocalStorageBackupMechanism = MutableLiveData<Boolean?>()
    val launchLocalStorageBackupMechanism: LiveData<Boolean?>
        get() = _launchLocalStorageBackupMechanism

    private val _launchRestoreMechanism = MutableLiveData<Boolean?>()
    val launchRestoreMechanism: LiveData<Boolean?>
        get() = _launchRestoreMechanism

    private val _openBackupMethodsDialogFragment = MutableLiveData<Boolean?>()
    val openBackupMethodsDialogFragment: LiveData<Boolean?>
        get() = _openBackupMethodsDialogFragment

    private val _openLocalStorageBackupDialogFragment = MutableLiveData<Boolean?>()
    val openLocalStorageBackupDialogFragment: LiveData<Boolean?>
        get() = _openLocalStorageBackupDialogFragment

    private val _openRestoreDialogFragment = MutableLiveData<Boolean?>()
    val openRestoreDialogFragment: LiveData<Boolean?>
        get() = _openRestoreDialogFragment

    fun doneShowingBackupMethodsDialogFragment() {
        _openBackupMethodsDialogFragment.value = false
    }

    fun doneShowingLocalStorageBackupDialogFragment() {
        _openLocalStorageBackupDialogFragment.value = false
    }

    fun doneShowingRestoreDialogFragment() {
        _openRestoreDialogFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun doneShowingSnackbarDataBackedUp() {
        _showSnackBarEventDataBackedUp.value = false
    }

    fun doneShowingSnackbarDataRestored() {
        _showSnackBarEventDataRestored.value = false
    }

    fun onBackupMechanismDone() {
        _launchLocalStorageBackupMechanism.value = false
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
            journeys.value?.forEach {
                val fileToDelete = File(it.coverPhotoSrcUri)
                if(fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllExperiences().value?.forEach{
                val fileToDelete = File(it.coverPhotoSrcUri)
                if(fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllMemories().value?.forEach{
                val fileToDelete = File(it.coverPhotoSrcUri)
                if(fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllMemoryPhotos().value?.forEach {
                val fileToDelete = File(it.photoSrcUri)
                if(fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.deletePhotos()
            database.deleteMemories()
            database.deleteExperiences()
            database.deleteJourneys()
        }
    }

    fun onJourneyClicked(id: Long) {
        _navigateToJourneyDetails.value = id
    }

    fun onJourneyDetailsNavigated() {
        _navigateToJourneyDetails.value = null
    }

    fun onBackupButtonClicked() {
        _openBackupMethodsDialogFragment.value = true
    }

    fun onLocalStorageBackupDialogOkButtonClicked() {
        _launchLocalStorageBackupMechanism.value = true
    }

    fun onDialogBackupMethodsLocalStorageClicked() {
        _openLocalStorageBackupDialogFragment.value = true
    }

    fun onRestoreButtonClicked() {
        _openRestoreDialogFragment.value = true
    }

    fun onDialogRestoreButtonClicked() {
        _launchRestoreMechanism.value = true
    }

    fun onLocalStorageBackup(context: Context, backupPath: String) {
        uiScope.launch {
            localStorageBackup(context, backupPath)
            _showSnackBarEventDataBackedUp.value = true
        }
    }

    private suspend fun localStorageBackup(context: Context, backupPath: String) {
        return withContext(Dispatchers.IO) {
            val dbFilePath = context.getDatabasePath(DATABASE_NAME).path
            val dbShmFilePath = context.getDatabasePath("$DATABASE_NAME-shm").path
            val dbWalFilePath = context.getDatabasePath("$DATABASE_NAME-wal").path

            val file = File(backupPath)
            if (!file.exists()) {
                file.mkdirs()
            }

            val dbFileExternalPath = backupPath + DATABASE_NAME
            val dbShmFileExternalPath = "$backupPath$DATABASE_NAME-shm"
            val dbWalFileExternalPath = "$backupPath$DATABASE_NAME-wal"

            try {
                copyFile(dbFilePath, dbFileExternalPath)
                copyFile(dbShmFilePath, dbShmFileExternalPath)
                copyFile(dbWalFilePath, dbWalFileExternalPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restore(context: Context, backupPath: String) {
        val dbFileExternalPath = backupPath + DATABASE_NAME
        val dbShmFileExternalPath = "$backupPath$DATABASE_NAME-shm"
        val dbWalFileExternalPath = "$backupPath$DATABASE_NAME-wal"

        val dbFilePath = context.getDatabasePath(DATABASE_NAME).path
        val dbShmFilePath = context.getDatabasePath("$DATABASE_NAME-shm").path
        val dbWalFilePath = context.getDatabasePath("$DATABASE_NAME-wal").path

        try {
            copyFile(dbFileExternalPath, dbFilePath)
            copyFile(dbShmFileExternalPath, dbShmFilePath)
            copyFile(dbWalFileExternalPath, dbWalFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _showSnackBarEventDataRestored.value = true
    }

    private fun copyFile(fromPath: String, toPath: String) {
        val inStream = File(fromPath).inputStream()
        val outStream = FileOutputStream(toPath)
        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }

        inStream.close()
        outStream.close()
    }

    fun onExternalStorageBackup(directory: String, zipFile: String, context: Context) {
        uiScope.launch {
            localStorageBackup(context, getBackupPath(context))
            externalStorageBackup(directory, zipFile)
        }
    }

    private suspend fun externalStorageBackup(directory: String, zipFile: String) {
        return withContext(Dispatchers.IO) {
            val sourceFile = File(directory)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { it ->
                it.use {
                    zipFiles(it, sourceFile, "")
                }
            }
        }
    }

    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val data = ByteArray(2048)

        for(f in sourceFile.listFiles()!!) {
            if(f.isDirectory) {
                val entry = ZipEntry(f.name + File.separator)
                entry.time = f.lastModified()
                entry.isDirectory
                entry.size = f.length()

                zipOut.putNextEntry(entry)

                zipFiles(zipOut, f, f.name)
            } else {
                if (!f.name.contains(".zip")) {
                    FileInputStream(f).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val path = parentDirPath + File.separator + f.name
                            val entry = ZipEntry(path)
                            entry.time = f.lastModified()
                            entry.isDirectory
                            entry.size = f.length()
                            zipOut.putNextEntry(entry)
                            while (true) {
                                val readBytes = origin.read(data)
                                if (readBytes == -1) {
                                    break
                                }
                                zipOut.write(data, 0, readBytes)
                            }
                        }
                    }
                } else {
                    zipOut.closeEntry()
                    zipOut.close()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}