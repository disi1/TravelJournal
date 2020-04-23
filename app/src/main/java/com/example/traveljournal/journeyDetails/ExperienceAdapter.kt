package com.example.traveljournal.journeyDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience

class ExperienceAdapter : RecyclerView.Adapter<ExperienceAdapter.ViewHolder>() {
    var data = listOf<Experience>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val experienceName: TextView = itemView.findViewById(R.id.experience_name)
        val experiencePlaceName: TextView = itemView.findViewById(R.id.experience_place_name)
        val experiencePlaceAddress: TextView = itemView.findViewById(R.id.experience_place_address)
        val experienceImage: ImageView = itemView.findViewById(R.id.experience_image)

        fun bind(
            item: Experience
        ) {
            experienceName.text = item.experienceName
            experiencePlaceName.text = item.experiencePlaceName
            experiencePlaceAddress.text = item.experiencePlaceAddress
            experienceImage.setImageResource(R.drawable.ic_undraw_destinations)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item_experience, parent, false)
                return ViewHolder(view)
            }
        }
    }
}