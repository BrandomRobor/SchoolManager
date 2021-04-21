package me.brandom.schoolmanager.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.databinding.FragmentHomeBinding

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
        val adapter = HomeworkListAdapter()

        binding.apply {
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    if (it is HomeworkViewModel.HomeworkRetrievalState.Success) {
                        fragmentHomeProgressBar.isVisible = false

                        if (it.homeworkList.isEmpty()) {
                            fragmentHomeRecyclerView.isVisible = false
                            fragmentHomeNoItemsMessage.isVisible = true
                        } else {
                            fragmentHomeNoItemsMessage.isVisible = false
                            fragmentHomeRecyclerView.isVisible = true
                            adapter.submitList(it.homeworkList)
                        }
                    }
                }
            }

            fragmentHomeAddFab.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToAddHomeworkFragment()
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}