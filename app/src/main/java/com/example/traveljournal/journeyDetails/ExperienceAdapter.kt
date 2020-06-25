package com.example.traveljournal.journeyDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Experience
import com.example.traveljournal.database.Memory
import com.example.traveljournal.databinding.ListItemExperienceBinding
import com.example.traveljournal.journeys.JourneysFragmentDirections

class ExperienceAdapter() : ListAdapter<Experience, ExperienceAdapter.ViewHolder>(ExperienceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class ViewHolder private constructor(val binding: ListItemExperienceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Experience
        ) {
            binding.clickListener = View.OnClickListener {
                val destination = JourneyDetailsFragmentDirections.actionJourneyDetailsDestinationToExperienceDetailsDestination(item.experienceId)
                val extras = FragmentNavigatorExtras(
                    binding.experienceImage to binding.experienceImage.transitionName,
                    binding.experienceName to binding.experienceName.transitionName,
                    binding.experiencePlaceName to binding.experiencePlaceName.transitionName,
                    binding.experiencePlaceAddress to binding.experiencePlaceAddress.transitionName,
                    binding.locationIcon to binding.locationIcon.transitionName,
                    binding.experienceDescription to binding.experienceDescription.transitionName
                )
                it.findNavController().navigate(destination, extras)
            }
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
