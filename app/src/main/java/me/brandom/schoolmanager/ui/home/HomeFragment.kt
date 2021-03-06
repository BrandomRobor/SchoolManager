package me.brandom.schoolmanager.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.databinding.FragmentHomeBinding
import me.brandom.schoolmanager.receivers.HomeworkReminderReceiver
import me.brandom.schoolmanager.ui.MainActivity

@AndroidEntryPoint
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

        binding.apply {
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

            retrievalStateJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    fragmentHomeProgressBar.isVisible =
                        it is HomeworkSharedViewModel.HomeworkRetrievalState.Loading
                    fragmentHomeRecyclerView.isVisible =
                        it is HomeworkSharedViewModel.HomeworkRetrievalState.Success && it.homeworkList.isNotEmpty()
                    fragmentHomeNoItemsMessage.isVisible =
                        it is HomeworkSharedViewModel.HomeworkRetrievalState.Success && it.homeworkList.isEmpty()
                    adapter.submitList((it as? HomeworkSharedViewModel.HomeworkRetrievalState.Success)?.homeworkList)
                }
            }

            fragmentHomeAddFab.setOnClickListener {
                viewModel.onAddHomeworkClick()
            }
        }

        homeworkEventsJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeworkEvents.collect {
                when (it) {
                    is HomeworkSharedViewModel.HomeworkEvents.CanEnterForm -> {
                        viewModel.resetStates()
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment(
                                getString(R.string.title_create_homework)
                            )
                        findNavController().navigate(action)
                    }
                    is HomeworkSharedViewModel.HomeworkEvents.CannotEnterForm ->
                        Snackbar.make(view, R.string.error_no_subjects, Snackbar.LENGTH_SHORT)
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

    override fun deleteHomework(homework: Homework) {
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
            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment("Edit homework")
        findNavController().navigate(action)
    }

    private fun createAlarm(id: Int, deadline: Long) {
        val intent = Intent(requireContext(), HomeworkReminderReceiver::class.java)
        intent.putExtra("id", id)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            id,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!,
            AlarmManager.RTC_WAKEUP,
            deadline,
            pendingIntent
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        retrievalStateJob?.cancel()
        homeworkEventsJob?.cancel()
        _binding = null
    }

}