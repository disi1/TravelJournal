package com.example.traveljournal

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

fun getRealPath(data: Intent?, context: Context): File {
    val selectedImage = data?.data
    val cursor = context!!.contentResolver.query(
        selectedImage!!,
        arrayOf(MediaStore.Images.ImageColumns.DATA),
        null,
        null,
        null
    )
    cursor!!.moveToFirst()

    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
    val selectedImagePath = cursor.getString(idx)
    cursor.close()

    return File(selectedImagePath)
}

fun backupPhoto(srcFile: File, destFile: File, backupPhotoPath: String) {
    if (!File(backupPhotoPath).exists()) {
        File(backupPhotoPath).mkdirs()
    }

    if(!destFile.exists()) {
        try {
            copyFile(srcFile, destFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun copyFile(srcFile: File, destFile: File) {
    val inStream = FileInputStream(srcFile)
    val outStream = FileOutputStream(destFile)

    inStream.use { input ->
        outStream.use { output ->
            input.copyTo(output)
        }
    }

    inStream.close()
    outStream.close()
}

