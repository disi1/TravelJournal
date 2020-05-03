package com.example.traveljournal.memoryDetails

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.R
import com.example.traveljournal.database.Memory
import com.example.traveljournal.database.MemoryPhoto
import com.example.traveljournal.databinding.HeaderBinding
import com.example.traveljournal.databinding.ListItemMemoryPhotoBinding
import com.example.traveljournal.databinding.TextHeaderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.lang.ClassCastException

private const val ITEM_VIEW_TYPE_TEXT_HEADER = 0
private const val ITEM_VIEW_TYPE_HEADER = 1
private const val ITEM_VIEW_TYPE_ITEM = 2

class MemoryPhotoGridAdapter(val clickListener: MemoryPhotoListener, val memoryDetailsViewModel: MemoryDetailsViewModel) :
        ListAdapter<DataItem, RecyclerView.ViewHolder>(MemoryPhotoDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_TEXT_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_HEADER -> ImageViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TextHeader -> ITEM_VIEW_TYPE_TEXT_HEADER
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.MemoryPhotoItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun addHeaderAndSubmitList(list: List<MemoryPhoto>?) {
        adapterScope.launch {
            val items = when(list) {
                null -> listOf(DataItem.TextHeader) + listOf(DataItem.Header)
                else -> listOf(DataItem.TextHeader) + listOf(DataItem.Header) + list.map { DataItem.MemoryPhotoItem(it) }
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
            is TextViewHolder -> {
                holder.bind(memoryDetailsViewModel)
            }
            is ImageViewHolder -> {
                holder.bind(memoryDetailsViewModel)
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

//    class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
//        companion object {
//            fun from(parent: ViewGroup): ImageViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val view = layoutInflater.inflate(R.layout.header, parent, false)
//
//                return ImageViewHolder(view)
//            }
//        }
//    }

    class ImageViewHolder(val binding: HeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MemoryDetailsViewModel) {
            binding.memoryDetailsViewModel = item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderBinding.inflate(layoutInflater, parent, false)

                return ImageViewHolder(binding)
            }
        }
    }

    class TextViewHolder(val binding: TextHeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MemoryDetailsViewModel) {
            binding.memoryDetailsViewModel = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TextHeaderBinding.inflate(layoutInflater, parent, false)

                return TextViewHolder(binding)
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

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

    object TextHeader: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract  val id : Long
}