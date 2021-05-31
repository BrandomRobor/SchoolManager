package me.brandom.schoolmanager.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.databinding.FragmentHomeBinding
import me.brandom.schoolmanager.internal.receiver.HomeworkBroadcastReceiver

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeworkViewModel>()

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
        val homeworkManager = object : HomeworkListAdapter.HomeworkManager {
            override fun deleteHomework(homework: Homework) {
                viewModel.deleteHomework(homework)
                val manager =
                    ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
                val intent = Intent(requireContext(), HomeworkBroadcastReceiver::class.java)
                intent.putExtra("id", homework.hwId)
                manager?.cancel(PendingIntent.getBroadcast(requireContext(), 0, intent, 0))
            }
        }
        val adapter = HomeworkListAdapter(homeworkManager)

        binding.apply {
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    if (it is HomeworkViewModel.HomeworkRetrievalState.Success) {
                        fragmentHomeProgressBar.isVisible = false

                        if (it.homeworkExist) {
                            fragmentHomeNoItemsMessage.isVisible = false
                            fragmentHomeRecyclerView.isVisible = true
                            adapter.submitList(it.homeworkList)
                        } else {
                            fragmentHomeRecyclerView.isVisible = false
                            fragmentHomeNoItemsMessage.isVisible = true
                        }
                    }
                }
            }

            fragmentHomeAddFab.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.getSubjectCount().collect {
                        if (it > 0) {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToAddHomeworkFragment()
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Add a subject first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}