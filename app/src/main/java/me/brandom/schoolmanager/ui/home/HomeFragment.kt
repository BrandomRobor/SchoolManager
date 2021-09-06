package me.brandom.schoolmanager.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import me.brandom.schoolmanager.databinding.FragmentHomeBinding
import me.brandom.schoolmanager.receivers.HomeworkReminderReceiver
import me.brandom.schoolmanager.ui.MainActivity
import me.brandom.schoolmanager.utils.NotificationTimeHelper
import me.brandom.schoolmanager.utils.SortOrder
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeFragment : Fragment(), HomeworkListAdapter.HomeworkManager, ActionMode.Callback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeworkSharedViewModel by activityViewModels()
    private var homeworkEventsJob: Job? = null
    private var retrievalStateJob: Job? = null
    private var actionMode: ActionMode? = null
    private lateinit var tracker: SelectionTracker<Long>

    @Inject
    lateinit var notificationTimeHelper: NotificationTimeHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        val adapter = HomeworkListAdapter(this)

        setHasOptionsMenu(true)

        binding.apply {
            ViewGroupCompat.setTransitionGroup(fragmentHomeRecyclerView, true)
            ViewCompat.setTransitionName(fragmentHomeAddFab, "fab_to_form_transition")

            fragmentHomeRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    fragmentHomeRecyclerView.context,
                    (fragmentHomeRecyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

            tracker = SelectionTracker.Builder(
                "homework_selection",
                fragmentHomeRecyclerView,
                HomeworkKeyProvider(adapter),
                HomeworkDetailsLookup(fragmentHomeRecyclerView),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

            tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    if (actionMode == null) {
                        actionMode =
                            if (tracker.hasSelection()) requireActivity().startActionMode(this@HomeFragment) else null
                    } else {
                        if (!tracker.hasSelection()) actionMode!!.finish()
                        actionMode?.title = resources.getQuantityString(
                            R.plurals.title_items_selected,
                            tracker.selection.size(),
                            tracker.selection.size()
                        )
                    }
                }
            })

            adapter.tracker = tracker

            retrievalStateJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.homeworkList.collect {
                    fragmentHomeNoItemsMessage.isVisible = it.isEmpty()
                    fragmentHomeRecyclerView.isVisible = it.isNotEmpty()
                    adapter.submitList(it)
                }
            }

            fragmentHomeAddFab.setOnClickListener {
                viewModel.onAddHomeworkClick()
            }
        }

        homeworkEventsJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeworkEvents.collect {
                when (it) {
                    HomeworkSharedViewModel.HomeworkFormChecks.OK -> {
                        viewModel.resetStates()
                        val extras =
                            FragmentNavigatorExtras(binding.fragmentHomeAddFab to "fab_to_form_transition")
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment(
                                getString(R.string.title_create_homework)
                            )
                        findNavController().navigate(action, extras)
                    }
                    HomeworkSharedViewModel.HomeworkFormChecks.NO_SUBJECTS ->
                        Snackbar.make(view, R.string.error_no_subjects, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_add_subject) {
                                val action = HomeFragmentDirections.actionGlobalSubjectFormFragment(
                                    getString(R.string.title_create_subject)
                                )
                                findNavController().navigate(action)
                            }
                            .show()
                }
            }
        }

        setFragmentResultListener("formResult") { _, bundle ->
            createAlarm(
                bundle.getInt("id", 0),
                bundle.getLong("deadline", System.currentTimeMillis())
            )

            when (bundle.getInt("result")) {
                MainActivity.FORM_CREATE_OK_FLAG ->
                    Snackbar.make(view, R.string.success_homework_created, Snackbar.LENGTH_SHORT)
                        .show()
                MainActivity.FORM_EDIT_OK_FLAG ->
                    Snackbar.make(view, R.string.success_homework_updated, Snackbar.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_home_options, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val sortOptionChecked = when (viewModel.currentSortOrder) {
            SortOrder.BY_NAME -> R.id.options_sort_name_option
            SortOrder.BY_DEADLINE -> R.id.options_sort_deadline_option
        }

        menu.findItem(sortOptionChecked).isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.options_sort_name_option -> {
            viewModel.setSortOrder(SortOrder.BY_NAME)
            item.isChecked = true
            true
        }
        R.id.options_sort_deadline_option -> {
            viewModel.setSortOrder(SortOrder.BY_DEADLINE)
            item.isChecked = true
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onHomeworkClick(rootView: View, hwWthSubject: HomeworkWithSubject) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
        }

        viewModel.resetStates(hwWthSubject.homework, hwWthSubject.subject)
        val extras = FragmentNavigatorExtras(rootView to "homework_to_details_transition")
        val action = HomeFragmentDirections.actionHomeFragmentToHomeworkDetailsFragment()
        findNavController().navigate(action, extras)
    }

    override fun markAsCompleted(homework: Homework, checkBoxState: Boolean) {
        viewModel.updateHomework(homework.copy(isComplete = checkBoxState))

        if (checkBoxState) {
            cancelAlarm(homework.hwId)
        } else {
            createAlarm(homework.hwId, homework.deadline)
        }
    }

    override fun deleteHomework(homework: Homework) {
        cancelAlarm(homework.hwId)

        viewModel.deleteHomework(homework)
        Snackbar.make(requireView(), R.string.success_homework_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo_delete) {
                viewModel.onUndoHomeworkClick(homework)
            }
            .show()
    }

    override fun editHomework(homework: Homework) {
        viewModel.resetStates(homework)
        val action =
            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment(getString(R.string.title_edit_homework))
        findNavController().navigate(action)
    }

    private fun createPendingIntent(id: Int): PendingIntent {
        val intent = Intent(requireContext(), HomeworkReminderReceiver::class.java)
        intent.putExtra("id", id)

        return PendingIntent.getBroadcast(
            requireContext(),
            id,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT else PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun createAlarm(id: Int, deadline: Long) {
        if (deadline > System.currentTimeMillis()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!,
                AlarmManager.RTC_WAKEUP,
                notificationTimeHelper.getInitialNotificationTime(deadline),
                createPendingIntent(id)
            )
        }
    }

    private fun cancelAlarm(id: Int) {
        val alarmManager =
            ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!
        alarmManager.cancel(createPendingIntent(id))
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        mode.menuInflater.inflate(R.menu.homework_list_selection_actions, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val ty = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorOnSurface, ty, true)
        DrawableCompat.setTint(menu.findItem(R.id.homework_list_selection_delete).icon, ty.data)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
        R.id.homework_list_selection_delete -> {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToDeleteBulkHomeworkDialogFragment(
                    tracker.selection.toList().toLongArray()
                )
            )
            mode.finish()
            true
        }
        else -> false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        tracker.clearSelection()
        actionMode = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        retrievalStateJob?.cancel()
        homeworkEventsJob?.cancel()
        _binding = null
    }

}