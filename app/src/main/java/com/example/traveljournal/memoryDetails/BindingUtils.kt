package com.example.traveljournal.memoryDetails

import android.text.SpannableString
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.MemoryPhoto
import java.io.File

@BindingAdapter("memoryImageUrl")
fun setMemoryImage(imageView: ImageView, item: MemoryPhoto?) {
    item?.let {
        val imageUri = item.photoSrcUri.toUri()
        Glide.with(imageView.context)
            .load(File(imageUri.path!!))
            .into(imageView)
    }
}

@BindingAdapter("memoryPhotoCaption")
fun TextView.setMemoryPhotoCaption(item: MemoryPhoto?) {
    item?.let {
        text = if(item.photoCaption != "") {
            item.photoCaption
        } else {
            resources.getString(R.string.add_a_caption_here)
        }
    }
}

@BindingAdapter("editTextMemoryPhotoCaption")
fun TextView.setMemoryPhotoCaptionOnEditText(item: MemoryPhoto?) {
    item?.let {
        text = if(item.photoCaption != "") {
            item.photoCaption
        } else {
            ""
        }
    }
}