package com.example.traveljournal.journeys

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.ListItemJourneyBinding


class JourneyAdapter(
    val clickListener: JourneyListener,
    val longClickListener: JourneyLongClickListener
): ListAdapter<Journey, JourneyAdapter.ViewHolder>(JourneyDiffCallback()) {

//    @TODO longClickListener for RecyclerView items does not work

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, longClickListener)
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
}

class JourneyDiffCallback: DiffUtil.ItemCallback<Journey>() {
    override fun areItemsTheSame(oldItem: Journey, newItem: Journey): Boolean {
        return oldItem.journeyId == newItem.journeyId
    }

    override fun areContentsTheSame(oldItem: Journey, newItem: Journey): Boolean {
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