package com.example.traveljournal.experienceDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Memory
import com.example.traveljournal.databinding.HeaderExperienceDescriptionBinding
import com.example.traveljournal.databinding.ListItemMemoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_DESCRIPTION_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class MemoryAdapter(val clickListener: MemoryListener, val experienceDetailsViewModel: ExperienceDetailsViewModel) : ListAdapter<DataItem, RecyclerView.ViewHolder>(MemoryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_DESCRIPTION_HEADER -> DescriptionHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.MemoryItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.DescriptionHeader -> ITEM_VIEW_TYPE_DESCRIPTION_HEADER
        }
    }

    fun addHeaderAndSubmitList(list: List<Memory>?) {
        adapterScope.launch {
            val items = when(list) {
                null -> listOf(DataItem.DescriptionHeader)
                else -> listOf(DataItem.DescriptionHeader) + list.map { DataItem.MemoryItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val memoryItem = getItem(position) as DataItem.MemoryItem
                holder.bind(memoryItem.memory, clickListener)
            }
            is DescriptionHeaderViewHolder -> {
                holder.bind(experienceDetailsViewModel)
            }
        }
    }

    class ViewHolder private constructor(val binding: ListItemMemoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Memory,
            clickListener: MemoryListener
        ) {
            binding.memory = item
            binding.clickListener = clickListener
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

class DescriptionHeaderViewHolder(val binding: HeaderExperienceDescriptionBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ExperienceDetailsViewModel) {
        binding.experienceDetailsViewModel = item
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): DescriptionHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = HeaderExperienceDescriptionBinding.inflate(layoutInflater, parent, false)

            return DescriptionHeaderViewHolder(binding)
        }
    }
}

class MemoryDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class MemoryListener(val clickListener: (memoryId: Long) -> Unit) {
    fun onClick(memory: Memory) = clickListener(memory.memoryId)
}

sealed class DataItem {
    data class MemoryItem(val memory: Memory) : DataItem() {
        override val id = memory.memoryId
    }

    object DescriptionHeader: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract  val id : Long
}