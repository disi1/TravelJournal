package com.example.traveljournal.journeyDetails

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience

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
fun ImageView.setExperienceImage(item: Experience?) {
    item?.let {
        setImageResource(R.drawable.ic_undraw_experience)
    }
}

@BindingAdapter("experienceDescription")
fun TextView.setExperienceDescription(item: Experience?) {
    item?.let {
        text = item.experienceDescription
    }
}