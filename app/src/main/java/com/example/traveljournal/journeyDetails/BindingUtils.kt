package com.example.traveljournal.journeyDetails

import android.util.Log
import android.view.View
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
        if (item.coverPhotoSrcUri == "") {
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

@BindingAdapter("experienceNameVisibility")
fun TextView.setExperienceNameVisibility(item: Experience?) {
    item?.let {
        visibility = if (it.experienceName == "") {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }
}

@BindingAdapter("experienceDescriptionVisibility")
fun TextView.setExperienceDescriptionVisibility(item: Experience?) {
    item?.let {
        visibility = if (it.experienceDescription == "") {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }
}

@BindingAdapter("experiencePlaceNameVisibility")
fun TextView.setExperiencePlaceNameVisibility(item: Experience?) {
    item?.let {
        visibility = if (it.experiencePlaceName == "") {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}

@BindingAdapter("experiencePlaceAddressVisibility")
fun TextView.setExperiencePlaceAddressVisibility(item: Experience?) {
    item?.let {
        visibility = if (it.experiencePlaceAddress == "") {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}

@BindingAdapter("experienceLocationIconVisibility")
fun ImageView.setExperienceLocationIconVisibility(item: Experience?) {
    item?.let {
        visibility = if (it.experiencePlaceAddress == "") {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }
}