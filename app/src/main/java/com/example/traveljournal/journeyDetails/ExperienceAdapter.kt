package com.example.traveljournal.journeyDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.database.Experience
import com.example.traveljournal.databinding.ListItemExperienceBinding

class ExperienceAdapter : ListAdapter<Experience, ExperienceAdapter.ViewHolder>(ExperienceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder private constructor(val binding: ListItemExperienceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Experience
        ) {
            binding.experience = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemExperienceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ExperienceDiffCallback : DiffUtil.ItemCallback<Experience>() {
    override fun areItemsTheSame(oldItem: Experience, newItem: Experience): Boolean {
        return oldItem.experienceId == newItem.experienceId
    }

    override fun areContentsTheSame(oldItem: Experience, newItem: Experience): Boolean {
        return oldItem == newItem
    }

}