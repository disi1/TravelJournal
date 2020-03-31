package com.example.traveljournal

import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.example.traveljournal.database.Journey
import kotlin.text.StringBuilder

fun formatJourneys(journeys: List<Journey>, resources: Resources): Spanned {
    val sb = StringBuilder()
    sb.apply {
        journeys.forEach {
            append("<br>")
            append(it.placeId)
            append("<br><br>")
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}