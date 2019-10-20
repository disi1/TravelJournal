package com.example.traveljournal


import android.os.Bundle
import android.text.Layout
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.traveljournal.databinding.FragmentNewJourneyBinding

class NewJourney : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.createJourney)

        val binding: FragmentNewJourneyBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_new_journey, container, false
        )
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.create_journey_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.createJourneyButton) {
            Toast.makeText(this.context, "Item Clicked", Toast.LENGTH_LONG).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
