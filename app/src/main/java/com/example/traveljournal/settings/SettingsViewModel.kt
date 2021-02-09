package com.example.traveljournal.settings

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.traveljournal.R
import com.example.traveljournal.database.Notification
import com.example.traveljournal.database.TravelDatabaseDao
import com.example.traveljournal.getBackupPath
import com.example.traveljournal.settings.receiver.AlarmReceiver
import com.example.traveljournal.settings.util.cancelNotifications
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val DATABASE_NAME = "travel_history_database"
private const val REQUEST_CODE = 0
private const val TRIGGER_TIME = "TRIGGER_AT"

class SettingsViewModel(
    val database: TravelDatabaseDao,
    private val app: Application
) : AndroidViewModel(app) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val journeys = database.getAllJourneys()

    private val backupNotification = MutableLiveData<Notification?>()

    private val second: Long = 1_000L
    private val day: Long = 86_400_000L

    private val timerLengthOptions: IntArray
    private val notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    private val _timeSelection = MutableLiveData<Int>()
    val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _openBackupDialogFragment = MutableLiveData<Boolean?>()
    val openBackupDialogFragment: LiveData<Boolean?>
        get() = _openBackupDialogFragment

    private val _openDeleteDataDialogFragment = MutableLiveData<Boolean?>()
    val openDeleteDataDialogFragment: LiveData<Boolean?>
        get() = _openDeleteDataDialogFragment

    private var _showDataDeletedSnackbarEvent = MutableLiveData<Boolean>()
    val showDataDeletedSnackbarEvent: LiveData<Boolean>
        get() = _showDataDeletedSnackbarEvent

    private val _navigateToJourneys = MutableLiveData<Boolean?>()
    val navigateToJourneys: LiveData<Boolean?>
        get() = _navigateToJourneys

    init {
        initializeBackupNotification()

        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        timerLengthOptions = app.resources.getIntArray(R.array.days_array)
    }

    private fun initializeBackupNotification() {
        uiScope.launch {
            val notification = getLatestBackupNotificationFromDb()
            if (notification == null) {
                val backupNotification = Notification()
                backupNotification.notificationType = "backup"
                backupNotification.notificationState = false

                insertNotification(backupNotification)
            } else {
                backupNotification.value = notification
                _alarmOn.value = backupNotification.value!!.notificationState
                _timeSelection.value = when (backupNotification.value!!.notificationIntervalMs) {
                    10_000L -> 0
                    1_296_000_000L -> 1
                    2_592_000_000 -> 2
                    5_184_000_000 -> 3
                    else -> 0
                }
            }
        }
    }

    private suspend fun getLatestBackupNotificationFromDb(): Notification? {
        return withContext(Dispatchers.IO) {
            database.getBackupNotification("backup")
        }
    }

    fun onCreateOrUpdateBackupNotification(isChecked: Boolean) {
        uiScope.launch {
            if (backupNotification.value == null) {
                val selectedTimeInterval = when (timeSelection.value) {
                    0 -> second * 10
                    else -> timerLengthOptions[timeSelection.value!!] * day
                }

                val notification = Notification()
                notification.notificationType = "backup"
                notification.notificationState = isChecked
                notification.notificationIntervalMs = selectedTimeInterval

                insertNotification(notification)

                backupNotification.value = getLatestBackupNotificationFromDb()
                setAlarm(isChecked, selectedTimeInterval)
            } else {
                val oldNotification = backupNotification.value ?: return@launch

                val selectedTimeInterval = when (timeSelection.value) {
                    0 -> second * 10
                    else -> timerLengthOptions[timeSelection.value!!] * day
                }
                oldNotification.notificationIntervalMs = selectedTimeInterval
                oldNotification.notificationState = isChecked

                updateNotification(oldNotification)

                backupNotification.value = getLatestBackupNotificationFromDb()
                setAlarm(isChecked, selectedTimeInterval)
            }
        }
    }

    private suspend fun insertNotification(notification: Notification) {
        withContext(Dispatchers.IO) {
            database.insertNotification(notification)
        }
    }

    private suspend fun updateNotification(notification: Notification) {
        withContext(Dispatchers.IO) {
            database.updateNotification(notification)
        }
    }

    private fun setAlarm(isChecked: Boolean, selectedTimeInterval: Long) {
        when (isChecked) {
            true -> startTimer(selectedTimeInterval)
            false -> cancelNotification()
        }
    }

    fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }

    private fun startTimer(selectedTimeInterval: Long) {
        _alarmOn.value?.let {
            if (!it) {
                _alarmOn.value = true
                val triggerTime = SystemClock.elapsedRealtime() + selectedTimeInterval

                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    selectedTimeInterval,
                    notifyPendingIntent
                )
            }
        }
    }

    private fun cancelNotification() {
        val notificationManager = ContextCompat.getSystemService(
            app,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()
        _alarmOn.value = false
        alarmManager.cancel(notifyPendingIntent)
    }

    fun doneNavigatingToJourneys() {
        _navigateToJourneys.value = null
    }

    fun doneShowingBackupDialogFragment() {
        _openBackupDialogFragment.value = false
    }

    fun doneShowingDeleteDataDialogFragment() {
        _openDeleteDataDialogFragment.value = false
    }

    fun doneShowingDataDeletedSnackbar() {
        _showDataDeletedSnackbarEvent.value = false
    }

    fun onBackupButtonClicked() {
        _openBackupDialogFragment.value = true
    }

    fun onDeleteDataButtonClicked() {
        _openDeleteDataDialogFragment.value = true
    }

    fun onDeleteData(backupPath: String) {
        uiScope.launch {
            clear(backupPath)
            _showDataDeletedSnackbarEvent.value = true
            _navigateToJourneys.value = true
        }
    }

    private suspend fun clear(backupPath: String) {
        withContext(Dispatchers.IO) {
            journeys.value?.forEach {
                val fileToDelete = File(it.coverPhotoSrcUri)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllExperiences().value?.forEach {
                val fileToDelete = File(it.coverPhotoSrcUri)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllMemories().value?.forEach {
                val fileToDelete = File(it.coverPhotoSrcUri)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.getAllMemoryPhotos().value?.forEach {
                val fileToDelete = File(it.photoSrcUri)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }

            database.deletePhotos()
            database.deleteMemories()
            database.deleteExperiences()
            database.deleteJourneys()

            val backupStorageDir = File(backupPath)
            if (backupStorageDir.exists()) {
                try {
                    backupStorageDir.deleteRecursively()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

    private fun zipFiles(
        zipOutputStream: ZipOutputStream,
        sourceFile: File,
        parentDirPath: String
    ) {
        val buffer = ByteArray(2048)
        sourceFile.listFiles()?.forEach { file ->
            if (file.isDirectory) {
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