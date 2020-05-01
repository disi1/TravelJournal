package com.example.traveljournal.experienceDetails

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
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