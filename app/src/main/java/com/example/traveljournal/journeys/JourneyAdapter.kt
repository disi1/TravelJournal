package com.example.traveljournal.journeys

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.ListItemJourneyBinding
import java.util.*


class JourneyAdapter(
    var journeysList: List<Journey>?,
    val clickListener: JourneyListener,
    val longClickListener: JourneyLongClickListener
): ListAdapter<Journey, JourneyAdapter.ViewHolder>(JourneyDiffCallback()), Filterable {

//    @TODO longClickListener for RecyclerView items does not work

    var journeysFilteredList: List<Journey>?

    init {
        journeysFilteredList = journeysList
    }

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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if(charSearch.isEmpty()) {
                    journeysFilteredList = journeysList
                } else {
                    journeysList?.let {
                        val resultList = arrayListOf<Journey>()
                        for(journey in journeysList!!) {
                            if ((journey.placeName.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) || (journey.placeAddress.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)))) {
                                resultList.add(journey)
                            }
                        }
                        journeysFilteredList = resultList
                    }
                }
                val filterResults = FilterResults()

                filterResults.values = journeysFilteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                journeysFilteredList = results?.values as List<Journey>?
                submitList(journeysFilteredList)
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