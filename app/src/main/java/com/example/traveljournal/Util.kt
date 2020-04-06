package com.example.traveljournal

import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Journey
import kotlin.text.StringBuilder

fun formatJourneys(journeys: List<Journey>, resources: Resources): Spanned {
    val sb = StringBuilder()
    sb.apply {
        journeys.forEach {
            append("Journey to ${it.placeName}")
            append("<br><br>")
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}

class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)