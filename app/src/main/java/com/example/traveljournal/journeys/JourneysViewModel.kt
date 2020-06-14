package com.example.traveljournal.journeys

import android.R.attr
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.traveljournal.database.TravelDatabaseDao
import com.example.traveljournal.getBackupPath
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


private const val DATABASE_NAME = "travel_history_database"

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val journeys = database.getAllJourneys()

    val backupFilePath = MutableLiveData<String>()

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    private val _navigateToSettings = MutableLiveData<Boolean?>()
    val navigateToSettings: LiveData<Boolean?>
        get() = _navigateToSettings

    private val _navigateToNewJourney = MutableLiveData<Boolean?>()
    val navigateToNewJourney: LiveData<Boolean?>
        get() = _navigateToNewJourney

    private val _navigateToJourneyDetails = MutableLiveData<Long>()
    val navigateToJourneyDetails: LiveData<Long>
        get() = _navigateToJourneyDetails

    private val _openBackupDialogFragment = MutableLiveData<Boolean?>()
    val openBackupDialogFragment: LiveData<Boolean?>
        get() = _openBackupDialogFragment

    private val _openRestoreDialogFragment = MutableLiveData<Boolean?>()
    val openRestoreDialogFragment: LiveData<Boolean?>
        get() = _openRestoreDialogFragment

    private val _openRestoreGuideDialogFragment = MutableLiveData<Boolean?>()
    val openRestoreGuideDialogFragment: LiveData<Boolean?>
        get() = _openRestoreGuideDialogFragment

    fun doneShowingBackupDialogFragment() {
        _openBackupDialogFragment.value = false
    }

    fun doneShowingRestoreDialogFragment() {
        _openRestoreDialogFragment.value = false
    }

    fun doneShowingRestoreGuideDialogFragment() {
        _openRestoreGuideDialogFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun doneNavigating() {
        _navigateToNewJourney.value = null
    }

    fun onNewJourney() {
        _navigateToNewJourney.value = true
    }

    fun onNavigateToSettings() {
        _navigateToSettings.value = true
    }

    fun onDoneNavigatingToSettings() {
        _navigateToSettings.value = false
    }

    fun onClear(backupPath: String) {
        uiScope.launch {
            clear(backupPath)
            _showSnackbarEvent.value = true
        }
    }

    private suspend fun clear(backupPath: String) {
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

            val backupStorageDir = File(backupPath)
            if(backupStorageDir.exists()) {
                try {
                    backupStorageDir.deleteRecursively()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onJourneyDetailsNavigated() {
        _navigateToJourneyDetails.value = null
    }

    fun onBackupButtonClicked() {
        _openBackupDialogFragment.value = true
    }

    fun onRestoreButtonClicked() {
        _openRestoreGuideDialogFragment.value = true
    }

    fun onRestoreMechanismInitialized() {
        _openRestoreDialogFragment.value = true
    }

    fun onRestore(context: Context, backupStoragePath: String) {
        unzipFile(backupFilePath.value.toString(), backupStoragePath, context)
        restoreDB(context, backupStoragePath)
    }

    private fun unzipFile(backupFilePath: String, backupStoragePath: String, context: Context) {
        val backupStorageDir = File(backupStoragePath)
        if(backupStorageDir.exists()) {
            try {
                backupStorageDir.deleteRecursively()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!backupStorageDir.exists()) {
            backupStorageDir.mkdir()
        }

        val fileInputStream = context.contentResolver.openInputStream(backupFilePath.toUri())
        val zipInputStream = ZipInputStream(fileInputStream)

        var entry = zipInputStream.nextEntry
        while(entry != null) {
            val filePath: String = backupStoragePath + File.separator.toString() + entry.name
            if(entry.isDirectory) {
                val dir = File(filePath)
                dir.mkdirs()
            } else {
                val buffer = ByteArray(2048)

                val fileOutputStream = FileOutputStream(filePath)
                val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
                var read = 0
                while (zipInputStream.read(buffer).also { read = it } != -1) {
                    bufferedOutputStream.write(buffer, 0, read)
                }

                bufferedOutputStream.close()
                fileOutputStream.close()
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    private fun restoreDB(context: Context, backupPath: String) {
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
    }

    fun onExternalStorageBackup(directory: String, zipFile: String, context: Context) {
        localStorageBackup(context, getBackupPath(context))
        externalStorageBackup(directory, zipFile)
    }

    private fun localStorageBackup(context: Context, backupPath: String) {
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

    private fun externalStorageBackup(directory: String, zipFile: String) {
        val sourceFile = File(directory)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { it ->
            it.use {
                zipFiles(it, sourceFile, "")
            }
        }
    }

    private fun zipFiles(zipOutputStream: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val buffer = ByteArray(2048)
        sourceFile.listFiles()?.forEach { file ->
            if(file.isDirectory) {
                val zipEntry = ZipEntry(file.name + File.separator)
                zipEntry.time = file.lastModified()
                zipEntry.isDirectory
                zipEntry.size = file.length()

                zipOutputStream.putNextEntry(zipEntry)

                zipFiles(zipOutputStream, file, file.name)
            } else {
                if (!file.name.contains(".bdia")) {
                    val fileInputStream = FileInputStream(file)
                    val bufferedInputStream = BufferedInputStream(fileInputStream, buffer.size)
                    val path = parentDirPath + File.separator + file.name
                    val entry = ZipEntry(path)
                    entry.time = file.lastModified()
                    entry.isDirectory
                    entry.size = file.length()
                    zipOutputStream.putNextEntry(entry)
                    var read = 0
                    while (bufferedInputStream.read(buffer).also { read = it } != -1) {
                        zipOutputStream.write(buffer, 0, read)
                    }
                    bufferedInputStream.close()
                    zipOutputStream.closeEntry()
                } else {
                    zipOutputStream.closeEntry()
                    zipOutputStream.close()
                }
            }
        }
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}