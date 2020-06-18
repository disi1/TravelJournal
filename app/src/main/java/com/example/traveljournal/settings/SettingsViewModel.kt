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

class SettingsViewModel(
    val database: TravelDatabaseDao,
    private val app: Application) : AndroidViewModel(app) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val journeys = database.getAllJourneys()

    val backupNotification = MutableLiveData<Notification?>()

    private val TRIGGER_TIME = "TRIGGER_AT"
    private val REQUEST_CODE = 0

    private val second: Long = 1_000L
    private val day: Long = 86_400_000L

    private val timerLengthOptions: IntArray
    private lateinit var notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var preferences =
        app.getSharedPreferences("com.example.traveljournal", Context.MODE_PRIVATE)
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    private val _timeSelection = MutableLiveData<Int>()
    val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private val _openBackupDialogFragment = MutableLiveData<Boolean?>()
    val openBackupDialogFragment: LiveData<Boolean?>
        get() = _openBackupDialogFragment

    private val _openDeleteDataDialogFragment = MutableLiveData<Boolean?>()
    val openDeleteDataDialogFragment: LiveData<Boolean?>
        get() = _openDeleteDataDialogFragment

    private var _showDataDeletedSnackbarEvent = MutableLiveData<Boolean>()
    val showDataDeletedSnackbarEvent: LiveData<Boolean>
        get() = _showDataDeletedSnackbarEvent

    private lateinit var timer: CountDownTimer

    init {
        initializeBackupNotification()

        Log.i("svm", "init: backupNotification - ${backupNotification.value}")

//        _alarmOn.value = backupNotification.value?.notificationState

        Log.i("svm", "init0: alarmOn - ${backupNotification.value}")

//        Log.i("svm", "init: alarmOn - ${_alarmOn.value}")
//        _alarmOn.value = PendingIntent.getBroadcast(
//            getApplication(),
//            REQUEST_CODE,
//            notifyIntent,
//            PendingIntent.FLAG_NO_CREATE
//        ) != null
//        Log.i("svm", "init2: alarmOn - ${_alarmOn.value}")

        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.i("svm", "init3: alarmOn - ${_alarmOn.value}")

        timerLengthOptions = app.resources.getIntArray(R.array.days_array)

//        if (_alarmOn.value!!) {
//            createTimer()
//        }
    }

    private fun initializeBackupNotification() {
        uiScope.launch {
            val notification = getLatestBackupNotificationFromDb()
            Log.i("svm", "initializeBackupNotification: notification - $notification")
            if(notification == null) {
                Log.i("svm", "initializeBackupNotification: notification is null")
                val backupNotification = Notification()
                backupNotification.notificationType = "backup"
                backupNotification.notificationState = false

                insertNotification(backupNotification)
            } else {
                backupNotification.value = notification
                _alarmOn.value = backupNotification.value!!.notificationState
                Log.i("svm", "initializeBackupNotification: backupNotification.value - ${backupNotification.value}")
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
            if(backupNotification.value == null) {
                Log.i("svm", "notifications - null")
                val notification = Notification()
                notification.notificationType = "backup"
                notification.notificationState = isChecked

                insertNotification(notification)

                backupNotification.value = getLatestBackupNotificationFromDb()
                setAlarm(isChecked)
            } else {
                Log.i("svm", "notifications - diferit de null")
                val oldNotification = backupNotification.value ?: return@launch

                oldNotification.notificationState = isChecked

                updateNotification(oldNotification)

                backupNotification.value = getLatestBackupNotificationFromDb()
                Log.i("svm", "${backupNotification.value}")
                setAlarm(isChecked)
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

    fun setAlarm(isChecked: Boolean) {
        Log.i("svm", "setAlarm isChecked: ${isChecked.toString()}")
        when (isChecked) {
            true -> timeSelection.value?.let { startTimer(it) }
            false -> cancelNotification()
        }
    }

    fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }

    private fun startTimer(timerLengthSelection: Int) {
        _alarmOn.value?.let {
            if(!it) {
                _alarmOn.value = true

                val selectedInterval = when (timerLengthSelection) {
                    0 -> second * 5
                    else -> timerLengthOptions[timerLengthSelection] * day
                }
                Log.i("svm", "setAlarm isChecked: ${selectedInterval.toString()}")
                val triggerTime = SystemClock.elapsedRealtime() + selectedInterval

                alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    selectedInterval,
                    notifyPendingIntent
                )

                viewModelScope.launch {
                    saveTime(triggerTime)
                }
            }
        }
//        createTimer()
    }

    private fun createTimer() {
        viewModelScope.launch {
            val triggerTime = loadTime()
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
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

    private fun resetTimer() {
        timer.cancel()
        _elapsedTime.value = 0
        _alarmOn.value = false
    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            preferences.edit().putLong(TRIGGER_TIME, triggerTime).apply()
        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            preferences.getLong(TRIGGER_TIME, 0)
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