package com.example.traveljournal.memoryDetails

import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.traveljournal.R
import com.example.traveljournal.database.MemoryPhoto
import java.io.File

@BindingAdapter("memoryImageUrl")
fun setMemoryImage(imageView: ImageView, item: MemoryPhoto?) {
    item?.let {
        val imageUri = item.photoSrcUri.toUri()
        Glide.with(imageView.context)
            .load(File(imageUri.path))
            .into(imageView)
    }
}