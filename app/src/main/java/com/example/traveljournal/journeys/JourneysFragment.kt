package com.example.traveljournal.journeys


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.traveljournal.*
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.databinding.FragmentJourneysBinding
import com.google.android.material.snackbar.Snackbar

class JourneysFragment : Fragment() {
    private lateinit var journeysViewModel: JourneysViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.my_journeys)

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentJourneysBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journeys, container, false)

        // get a reference to the application that this fragment is attached to, to pass in to the ViewModelFactory provider
        val application = requireNotNull(this.activity).application

        // get a reference to the data source via a reference to the Dao
        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = JourneysViewModelFactory(dataSource, application)

        journeysViewModel = ViewModelProviders.of(this, viewModelFactory).get(JourneysViewModel::class.java)

        binding.journeysViewModel = journeysViewModel
        
        val manager = GridLayoutManager(activity, 2)
        manager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = when (position) {
                0 -> 2
                else -> 1
            }
        }
        binding.journeysList.layoutManager = manager

        val adapter = JourneyAdapter(
            JourneyListener {
                journeyId -> journeysViewModel.onJourneyClicked(journeyId)},
            JourneyLongClickListener {
                    journey ->  Toast.makeText(context, "Journey long-clicked", Toast.LENGTH_LONG).show()},
            journeysViewModel)
        binding.journeysList.adapter = adapter
        binding.lifecycleOwner = this

        val backupPath = context!!.getExternalFilesDir(null)!!.path + "/Backup/"

        journeysViewModel.journeys.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
                if(it.isNotEmpty()) {
                    manager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int) = when (position) {
                            0 -> 1
                            else -> 1
                        }
                    }
                } else {
                    manager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int) = when (position) {
                            0 -> 2
                            else -> 1
                        }
                    }
                }
            }
        })

        journeysViewModel.navigateToJourneyDetails.observe(viewLifecycleOwner, Observer { journey ->
            journey?.let {
                this.findNavController().navigate(
                    JourneysFragmentDirections.actionJourneysDestinationToJourneyDetailsDestination(journey))
                journeysViewModel.onJourneyDetailsNavigated()
            }
        })

        journeysViewModel.navigateToNewJourney.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                this.findNavController().navigate(
                    JourneysFragmentDirections
                        .actionJourneysDestinationToNewJourneyDestination())
                journeysViewModel.doneNavigating()
            }
        })

        journeysViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_journeys_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbar()
            }
        })

        journeysViewModel.showSnackBarEventJourneyDeleted.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.deleted_journey_message, "Honolulu"),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbarJourneyDeleted()
            }
        })

        journeysViewModel.openBackupMethodsDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = BackupMethodsDialogFragment(journeysViewModel)

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("backup_methods_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "backup_methods_dialog")
            }
        })

        journeysViewModel.openLocalStorageBackupDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = BackupDialogFragment(journeysViewModel, getBackupPath(context!!))

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("local_storage_backup_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "local_storage_backup_dialog")
            }
        })

        journeysViewModel.openRestoreDialogFragment.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                val dialogFragment = RestoreDialogFragment(journeysViewModel, getBackupPath(context!!))

                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("restore_dialog")

                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)

                dialogFragment.setTargetFragment(this, 300)

                dialogFragment.show(ft, "restore_dialog")
            }
        })

        journeysViewModel.launchLocalStorageBackupMechanism.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionChecker.checkSelfPermission(
                            context!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PermissionChecker.PERMISSION_DENIED
                    ) {
                        val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                        requestPermissions(permission, 9990) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                    } else {
                        journeysViewModel.localStorageBackup(context!!, getBackupPath(context!!))
                        journeysViewModel.onBackupMechanismDone()
                    }
                }
            }
        })

        journeysViewModel.launchRestoreMechanism.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionChecker.checkSelfPermission(
                            context!!,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PermissionChecker.PERMISSION_DENIED
                    ) {
                        val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                        requestPermissions(permission, 9990) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                    } else {
                        journeysViewModel.restore(context!!, backupPath)
                        journeysViewModel.onRestoreMechanismDone()
                        triggerRestart(context!!)
                    }
                }
            }
        })

        journeysViewModel.showSnackBarEventDataBackedUp.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.data_backed_up),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbarDataBackedUp()

            }
        })

        journeysViewModel.showSnackBarEventDataRestored.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.data_restored),
                    Snackbar.LENGTH_SHORT
                ).show()
                journeysViewModel.doneShowingSnackbarDataRestored()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.journeys_overflow_menu, menu)

        val deleteAllDataMenu = menu.findItem(R.id.delete_all_data_menu)
        val spannableString = SpannableString(deleteAllDataMenu.title.toString())
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.colorAccent)), 0, spannableString.length, 0)
        deleteAllDataMenu.title = spannableString

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.delete_all_data_menu) {
            val dialogFragment = DeleteAllDataDialogFragment(journeysViewModel)

            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("backup_methods_dialog")

            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            dialogFragment.setTargetFragment(this, 300)

            dialogFragment.show(ft, "backup_methods_dialog")

            return true
        }

        if(id == R.id.backup_data_menu) {
            journeysViewModel.onBackupButtonClicked()
            return true
        }

        if(id == R.id.restore_data_menu) {
            journeysViewModel.onRestoreButtonClicked()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun triggerRestart(context: Context) {
        val intent = Intent(context, JourneysActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Runtime.getRuntime().exit(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 9990) {
            journeysViewModel.onBackupButtonClicked()
        }
    }

//    private fun setRecyclerViewItemTouchListener(journey: Journey) {
//        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(
//            0,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                Toast.makeText(context, journey.placeName, Toast.LENGTH_LONG).show()
////                journeysViewModel.onDeleteJourney(journey)
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
//        itemTouchHelper.attachToRecyclerView()
//    }
}
