package com.example.traveljournal

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

fun getBackupPath(context: Context): String {
    return context.getExternalFilesDir(null)!!.path + "/Backup/"
}

fun getCurrentDate(): String {
    val c = Calendar.getInstance()

    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    return "$day-$month-$year"
}

fun getCurrentDateAndTime(): String {
    val date = Calendar.getInstance().time
    val formatter = SimpleDateFormat.getDateTimeInstance()
    return formatter.format(date)
}

