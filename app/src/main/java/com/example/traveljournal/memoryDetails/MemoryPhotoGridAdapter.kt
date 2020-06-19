package com.example.traveljournal.memoryDetails

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.databinding.HeaderMemoryDescriptionBinding
import com.example.traveljournal.databinding.ListItemMemoryPhotoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

private const val ITEM_VIEW_TYPE_DESCRIPTION_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class MemoryPhotoGridAdapter(
    val clickListener: MemoryPhotoListener,
    val memoryDetailsViewModel: MemoryDetailsViewModel,
    var memoryPhotosList: List<MemoryPhoto>?
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(MemoryPhotoDiffCallback()) {

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
            is DataItem.DescriptionHeader -> ITEM_VIEW_TYPE_DESCRIPTION_HEADER
            is DataItem.MemoryPhotoItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun addHeaderAndSubmitList(list: List<MemoryPhoto>?) {
        adapterScope.launch {
            val items = when(list) {
                null -> listOf(DataItem.DescriptionHeader)
                else -> listOf(DataItem.DescriptionHeader) + list.map { DataItem.MemoryPhotoItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val memoryPhotoItem = getItem(position) as DataItem.MemoryPhotoItem
                holder.bind(memoryPhotoItem.memoryPhoto, clickListener)
            }
            is DescriptionHeaderViewHolder -> {
                holder.bind(memoryDetailsViewModel, memoryPhotosList)
            }
        }
    }

    class ViewHolder private constructor(val binding: ListItemMemoryPhotoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MemoryPhoto,
            clickListener: MemoryPhotoListener
        ) {
            binding.memoryPhoto = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMemoryPhotoBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class DescriptionHeaderViewHolder(val binding: HeaderMemoryDescriptionBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MemoryDetailsViewModel, memoryPhotosList: List<MemoryPhoto>?) {
            if (memoryPhotosList != null) {
                if(memoryPhotosList.isNotEmpty()) {
                    binding.emptyMemoryPhotosListImage.visibility = View.GONE
                } else {
                    binding.emptyMemoryPhotosListImage.visibility = View.VISIBLE
                }
            }

            binding.memoryDetailsViewModel = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): DescriptionHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderMemoryDescriptionBinding.inflate(layoutInflater, parent, false)

                return DescriptionHeaderViewHolder(binding)
            }
        }
    }
}

class MemoryPhotoDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

}

class MemoryPhotoListener(val clickListener: (memoryPhoto: MemoryPhoto) -> Unit) {
    fun onClick(memoryPhoto: MemoryPhoto) = clickListener(memoryPhoto)
}

sealed class  DataItem {
    data class MemoryPhotoItem(val memoryPhoto: MemoryPhoto): DataItem(){
        override val id = memoryPhoto.photoId
    }

    object DescriptionHeader: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract  val id : Long
}