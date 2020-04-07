package com.example.traveljournal.journeys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.TextItemViewHolder
import com.example.traveljournal.database.Journey

class JourneyAdapter: RecyclerView.Adapter<JourneyAdapter.ViewHolder>() {
    var data = listOf<Journey>()

    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_journey, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = data[position]
        holder.journeyDestinationName.text = item.placeName
        holder.journeyDestinationAddress.text = item.placeAddress
        holder.journeyImage.setImageResource(R.drawable.ic_undraw_destinations)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val journeyDestinationName: TextView = itemView.findViewById(R.id.journey_destination_name)
        val journeyDestinationAddress: TextView = itemView.findViewById(R.id.journey_destination_address)
        val journeyImage: ImageView = itemView.findViewById(R.id.journey_image)
    }
}