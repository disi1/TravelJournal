package com.example.traveljournal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
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

    return "$day-${month + 1}-$year"
}

fun getCurrentDateAndTime(): String {
    val date = Calendar.getInstance().time
    val formatter = SimpleDateFormat.getDateTimeInstance()
    return formatter.format(date)
}

fun getRealPathForIntentData(data: Intent?, context: Context): File {
    val selectedImage = data?.data
    val cursor = context.contentResolver.query(
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

fun getRealPathFromUri(data: Uri?, context: Context): File {
    val cursor = data?.let {
        context.contentResolver.query(
            it,
        arrayOf(MediaStore.Images.ImageColumns.DATA),
        null,
        null,
        null
    )
    }
    cursor!!.moveToFirst()

    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
    val selectedImagePath = cursor.getString(idx)
    cursor.close()

    return File(selectedImagePath)
}

fun backupPhoto(srcFile: File, destFile: File, backupPhotoPath: String) {
    Log.i("mdf", "srcFile: $srcFile")
    Log.i("mdf", "destFile: $destFile")
    if (!File(backupPhotoPath).exists()) {
        File(backupPhotoPath).mkdirs()
    }

    if(!destFile.exists()) {
//        try {
            copyFile(srcFile, destFile)
//        } catch (e: Exception) {
//            Log.i("mdf", e.printStackTrace().toString())
//            e.printStackTrace()
//        }
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

fun saveBitmap(bitmapImage: Bitmap, imageName: String, backupPhotoPath: String): String {
    if (!File(backupPhotoPath).exists()) {
        File(backupPhotoPath).mkdirs()
    }

    val imageFile = File(backupPhotoPath, imageName)
    if(!imageFile.exists()) {
        try {
            val outStream = FileOutputStream(imageFile)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return imageFile.toString()
}

fun Fragment.waitForTransition(targetView: View) {
    postponeEnterTransition()
    targetView.doOnPreDraw { startPostponedEnterTransition() }
}

fun rotateFAB(view: View, rotate: Boolean): Boolean {
    view.animate().setDuration(200)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
            }
        })
        .rotation(if (rotate) 135f else 0f)
    return rotate
}

fun showIn(view: View) {
    view.visibility = View.VISIBLE
    view.alpha = 0f
    view.translationY = view.height.toFloat()

    view.animate()
        .setDuration(200)
        .translationY(0F)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }
        })
        .alpha(1f)
        .start()
}

fun showOut(view: View) {
    view.visibility = View.VISIBLE
    view.alpha = 1f
    view.translationY = 0F

    view.animate()
        .setDuration(200)
        .translationY(view.height.toFloat())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
                super.onAnimationEnd(animation)
            }
        })
        .alpha(0f)
        .start()
}

fun init(view: View) {
    view.visibility = View.GONE
    view.translationY = view.height.toFloat()
    view.alpha = 0F
}

//fun ImageView.setJourneyImage(item: Journey?, onLoadingFinished: () -> Unit = {}) {
//    item?.let {
//        if(item.coverPhotoSrcUri == "") {
//            this.setImageResource(R.drawable.ic_undraw_destinations)
//        } else {
//            val listener = object : RequestListener<Drawable> {
//                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                    onLoadingFinished()
//                    return false
//                }
//
//                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                    onLoadingFinished()
//                    return false
//                }
//            }
//            val imageUri = item.coverPhotoSrcUri.toUri()
//            Glide.with(this.context)
//                .load(File(imageUri.path!!))
//                .listener(listener)
//                .into(this)
//        }
//    }
//}


