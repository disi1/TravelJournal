package com.example.traveljournal.experienceDetails

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.example.traveljournal.R
import com.example.traveljournal.database.Memory
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
fun ImageView.setMemoryImage(item: Memory?) {
    item?.let {
        setImageResource(R.drawable.ic_undraw_memory)
    }
}

@BindingAdapter("visible")
fun TextView.setVisibility(item: Boolean) {
    item.let {
        visibility = when (item) {
            true -> View.VISIBLE
            false -> View.GONE
            else -> throw IllegalArgumentException("Unknown VISIBILITY type: $item")
        }
    }
}
