package me.brandom.schoolmanager.ui.subject

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
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.databinding.FragmentSubjectBinding

@AndroidEntryPoint
class SubjectFragment : Fragment() {
    private var _binding: FragmentSubjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SubjectViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SubjectListAdapter()

        binding.apply {
            fragmentSubjectRecyclerView.setHasFixedSize(true)
            fragmentSubjectRecyclerView.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    if (it is SubjectViewModel.SubjectRetrievalState.Success) {
                        if (it.subjectList.isEmpty()) {
                            fragmentSubjectRecyclerView.isVisible = false
                        } else {
                            fragmentSubjectRecyclerView.isVisible = true
                            adapter.submitList(it.subjectList)
                        }
                    }
                }
            }

            fragmentSubjectFab.setOnClickListener {
                val action = SubjectFragmentDirections.actionSubjectFragmentToAddSubjectFragment()
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}