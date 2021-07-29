package me.brandom.schoolmanager.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.databinding.FragmentHomeBinding
import me.brandom.schoolmanager.receivers.HomeworkReminderReceiver
import me.brandom.schoolmanager.ui.MainActivity
import me.brandom.schoolmanager.utils.SortOrder

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeFragment : Fragment(), HomeworkListAdapter.HomeworkManager {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeworkSharedViewModel by activityViewModels()
    private var homeworkEventsJob: Job? = null
    private var retrievalStateJob: Job? = null

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
        val adapter = HomeworkListAdapter(this)

        setHasOptionsMenu(true)

        binding.apply {
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

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
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment(
                                getString(R.string.title_create_homework)
                            )
                        findNavController().navigate(action)
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
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun createAlarm(id: Int, deadline: Long) {
        if (deadline > System.currentTimeMillis()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!,
                AlarmManager.RTC_WAKEUP,
                deadline,
                createPendingIntent(id)
            )
        }
    }

    private fun cancelAlarm(id: Int) {
        val alarmManager =
            ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!
        alarmManager.cancel(createPendingIntent(id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        retrievalStateJob?.cancel()
        homeworkEventsJob?.cancel()
        _binding = null
    }

}