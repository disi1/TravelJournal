package com.example.traveljournal.journeyDetails

import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience
import java.io.File

@BindingAdapter("experienceName")
fun TextView.setExperienceName(item: Experience?) {
    item?.let {
        text = item.experienceName
    }
}

@BindingAdapter("experiencePlaceName")
fun TextView.setExperiencePlaceName(item: Experience?) {
    item?.let {
        text = item.experiencePlaceName
    }
}

@BindingAdapter("experiencePlaceAddress")
fun TextView.setExperiencePlaceAddress(item: Experience?) {
    item?.let {
        text = item.experiencePlaceAddress
    }
}

@BindingAdapter("experienceImage")
fun setExperienceImage(imageView: ImageView, item: Experience?) {
    item?.let {
        if(item.coverPhotoSrcUri == "") {
            imageView.setImageResource(R.drawable.ic_undraw_experience)
        } else {
            val imageUri = item.coverPhotoSrcUri.toUri()
            Glide.with(imageView.context)
                .load(File(imageUri.path!!))
                .into(imageView)
        }
    }
}

@BindingAdapter("experienceDescription")
fun TextView.setExperienceDescription(item: Experience?) {
    item?.let {
        text = item.experienceDescription
    }
}