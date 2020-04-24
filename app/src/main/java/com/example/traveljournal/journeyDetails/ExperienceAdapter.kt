package com.example.traveljournal.journeyDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Experience
import com.example.traveljournal.databinding.ListItemExperienceBinding

class ExperienceAdapter(val clickListener: ExperienceListener) : ListAdapter<Experience, ExperienceAdapter.ViewHolder>(ExperienceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder private constructor(val binding: ListItemExperienceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Experience,
            clickListener: ExperienceListener
        ) {
            binding.experience = item
            binding.clickListener = clickListener
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

class ExperienceListener(val clickListener: (experienceId: Long) -> Unit) {
    fun onClick(experience: Experience) = clickListener(experience.experienceId)
}