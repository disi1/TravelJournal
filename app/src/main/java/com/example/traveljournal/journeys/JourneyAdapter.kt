package com.example.traveljournal.journeys

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.ListItemJourneyBinding

class JourneyAdapter: ListAdapter<Journey, JourneyAdapter.ViewHolder>(JourneyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder private constructor(val binding: ListItemJourneyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Journey) {
            binding.journey = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemJourneyBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class JourneyDiffCallback: DiffUtil.ItemCallback<Journey>() {
    override fun areItemsTheSame(oldItem: Journey, newItem: Journey): Boolean {
        return oldItem.journeyId == newItem.journeyId
    }

    override fun areContentsTheSame(oldItem: Journey, newItem: Journey): Boolean {
        return oldItem == newItem
    }

}