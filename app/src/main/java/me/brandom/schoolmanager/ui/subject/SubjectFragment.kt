package me.brandom.schoolmanager.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.databinding.FragmentSubjectBinding
import me.brandom.schoolmanager.ui.MainActivity

@AndroidEntryPoint
class SubjectFragment : Fragment() {
    private var _binding: FragmentSubjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SubjectViewModel>()
    private var retrievalStateJob: Job? = null

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

            retrievalStateJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    fragmentSubjectProgressBar.isVisible =
                        it is SubjectViewModel.SubjectRetrievalState.Loading
                    fragmentSubjectsNoItemsMessage.isVisible =
                        it is SubjectViewModel.SubjectRetrievalState.Success && it.subjectList.isEmpty()
                    fragmentSubjectRecyclerView.isVisible =
                        it is SubjectViewModel.SubjectRetrievalState.Success && it.subjectList.isNotEmpty()
                    adapter.submitList((it as? SubjectViewModel.SubjectRetrievalState.Success)?.subjectList)
                }
            }

            fragmentSubjectFab.setOnClickListener {
                val action =
                    SubjectFragmentDirections.actionSubjectFragmentToSubjectFormFragment(getString(R.string.title_create_subject))
                findNavController().navigate(action)
            }

            setFragmentResultListener("subjectFormResult") { _, bundle ->
                when (bundle.getInt("result")) {
                    MainActivity.FORM_CREATE_OK_FLAG ->
                        Snackbar.make(view, R.string.success_subject_created, Snackbar.LENGTH_SHORT)
                            .show()
                    MainActivity.FORM_EDIT_OK_FLAG ->
                        Snackbar.make(view, R.string.success_subject_updated, Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        retrievalStateJob?.cancel()
        _binding = null
    }
}