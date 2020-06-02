package com.example.traveljournal.experienceDetails

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.example.traveljournal.R
import com.example.traveljournal.database.Memory
import java.io.File
import java.text.DateFormat
import java.util.*

@BindingAdapter("memoryName")
fun TextView.setMemoryName(item: Memory?) {
    item?.let {
        text = item.memoryName
    }
}

@BindingAdapter("memoryDate")
fun TextView.setMemoryDate(item: Memory?) {
    item?.let {
        text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.memoryTimestamp))
    }
}

@BindingAdapter("memoryDescription")
fun TextView.setMemoryDescription(item: Memory?) {
    item?.let {
        text = item.memoryDescription
    }
}

@BindingAdapter("memoryImage")
fun setMemoryImage(imageView: ImageView, item: Memory?) {
    item?.let {
        if(item.coverPhotoSrcUri == "") {
            imageView.setImageResource(R.drawable.ic_undraw_memory)
        } else {
            val imageUri = item.coverPhotoSrcUri.toUri()
            Glide.with(imageView.context)
                .load(File(imageUri.path!!))
                .into(imageView)
        }
    }
}
