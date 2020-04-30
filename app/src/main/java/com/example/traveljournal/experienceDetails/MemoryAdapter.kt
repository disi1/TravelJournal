package com.example.traveljournal.experienceDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.database.Memory
import com.example.traveljournal.databinding.ListItemMemoryBinding
import java.text.DateFormat
import java.util.*

class MemoryAdapter : ListAdapter<Memory, MemoryAdapter.ViewHolder>(MemoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder private constructor(val binding: ListItemMemoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Memory) {
            binding.memory = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMemoryBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class MemoryDiffCallback : DiffUtil.ItemCallback<Memory>() {
    override fun areItemsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem.memoryId == newItem.memoryId
    }

    override fun areContentsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem == newItem
    }

}