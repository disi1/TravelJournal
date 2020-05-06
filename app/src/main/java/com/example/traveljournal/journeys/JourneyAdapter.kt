package com.example.traveljournal.journeys

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.HeaderEmptyJourneyListBinding
import com.example.traveljournal.databinding.ListItemJourneyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

private const val ITEM_VIEW_TYPE_EMPTY_LIST_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class JourneyAdapter(
    val clickListener: JourneyListener,
    val longClickListener: JourneyLongClickListener,
    val journeysViewModel: JourneysViewModel): ListAdapter<DataItem, RecyclerView.ViewHolder>(JourneyDiffCallback()) {

//    @TODO longClickListener for RecyclerView items does not work

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_VIEW_TYPE_EMPTY_LIST_HEADER -> EmptyListHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val journeyItem = getItem(position) as DataItem.JourneyItem
                holder.bind(journeyItem.journey, clickListener, longClickListener)
            }
            is EmptyListHeaderViewHolder -> {
                holder.bind(journeysViewModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.EmptyListHeader -> ITEM_VIEW_TYPE_EMPTY_LIST_HEADER
            is DataItem.JourneyItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun addHeaderAndSubmitList(list: List<Journey>?) {
        adapterScope.launch {
            val items = when(list) {
                null -> listOf(DataItem.EmptyListHeader)
                else -> list.map {DataItem.JourneyItem(it)}
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    class ViewHolder private constructor(val binding: ListItemJourneyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Journey,
            clickListener: JourneyListener,
            longClickListener: JourneyLongClickListener
        ) {
            binding.journey = item
            binding.clickListener = clickListener
            binding.longClickListener = longClickListener
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

    class EmptyListHeaderViewHolder(val binding: HeaderEmptyJourneyListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: JourneysViewModel) {
            binding.journeysViewModel = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EmptyListHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderEmptyJourneyListBinding.inflate(layoutInflater, parent, false)

                return EmptyListHeaderViewHolder(binding)
            }
        }
    }
}

class JourneyDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}

class JourneyListener(val clickListener: (journeyId: Long) -> Unit) {
    fun onClick(journey: Journey) = clickListener(journey.journeyId)
}

//class JourneyLongClickListener(val longClickListener: (journey: Journey) -> Unit) {
//    fun onLongClick(journey: Journey) = longClickListener(journey)
//}
class JourneyLongClickListener(val longClickListener: (Boolean) -> Unit) {
    fun onLongClick(boolean: Boolean) = longClickListener(boolean)
}

sealed class DataItem {
    data class JourneyItem(val journey: Journey): DataItem() {
        override val id = journey.journeyId
    }
    object EmptyListHeader: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}