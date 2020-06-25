package com.example.traveljournal.journeys

import android.app.Application
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.traveljournal.database.TravelDatabaseDao
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipInputStream


private const val DATABASE_NAME = "travel_history_database"

class JourneysViewModel(
        val database: TravelDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    val journeys = database.getAllJourneys()

    val backupFilePath = MutableLiveData<String>()

    private val _navigateToSettings = MutableLiveData<Boolean?>()
    val navigateToSettings: LiveData<Boolean?>
        get() = _navigateToSettings

    private val _navigateToNewJourney = MutableLiveData<Boolean?>()
    val navigateToNewJourney: LiveData<Boolean?>
        get() = _navigateToNewJourney

    private val _openRestoreDialogFragment = MutableLiveData<Boolean?>()
    val openRestoreDialogFragment: LiveData<Boolean?>
        get() = _openRestoreDialogFragment

    fun doneShowingRestoreDialogFragment() {
        _openRestoreDialogFragment.value = false
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