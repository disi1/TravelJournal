package com.example.traveljournal.journeys

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.traveljournal.R
import com.example.traveljournal.database.Journey
import java.io.File

@BindingAdapter("journeyDestinationName")
fun TextView.setJourneyDestinationName(item: Journey?) {
    item?.let {
        text = item.placeName
    }
}

@BindingAdapter("journeyDestinationAddress")
fun TextView.setJourneyDestinationAddress(item: Journey?) {
    item?.let {
        text = item.placeAddress
    }
}

@BindingAdapter("journeyImage")
fun setJourneyImage(imageView: ImageView, item: Journey?) {
    item?.let {
        if(item.coverPhotoSrcUri == "") {
            imageView.setImageResource(R.drawable.ic_undraw_journey)
        } else {
            val imageUri = item.coverPhotoSrcUri.toUri()
            Glide.with(imageView.context)
                .load(File(imageUri.path!!))
                .into(imageView)
        }
    }
}