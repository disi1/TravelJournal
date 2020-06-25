package com.example.traveljournal.journeys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.Journey
import com.example.traveljournal.databinding.ListItemJourneyBinding
import java.util.*


class JourneyAdapter(
    var journeysList: List<Journey>?
): ListAdapter<Journey, JourneyAdapter.ViewHolder>(JourneyDiffCallback()), Filterable {

    var journeysFilteredList: List<Journey>?

    init {
        journeysFilteredList = journeysList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class ViewHolder private constructor(val binding: ListItemJourneyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Journey
        ) {
            binding.clickListener = View.OnClickListener {
                val destination = JourneysFragmentDirections.actionJourneysDestinationToJourneyDetailsDestination(item.journeyId)
                val extras = FragmentNavigatorExtras(
                    binding.journeyDestinationName to binding.journeyDestinationName.transitionName,
                    binding.journeyDestinationAddress to binding.journeyDestinationAddress.transitionName,
                    binding.journeyImage to binding.journeyImage.transitionName,
                    binding.locationIcon to binding.locationIcon.transitionName
                )
                it.findNavController().navigate(destination, extras)
            }
            binding.journey = item
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