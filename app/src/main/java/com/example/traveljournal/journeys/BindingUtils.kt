package com.example.traveljournal.journeys

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.traveljournal.R
import com.example.traveljournal.database.Journey

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
fun ImageView.setJourneyImage(item: Journey?) {
    item?.let {
        setImageResource(R.drawable.ic_undraw_destinations)
    }
}