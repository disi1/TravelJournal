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

class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)