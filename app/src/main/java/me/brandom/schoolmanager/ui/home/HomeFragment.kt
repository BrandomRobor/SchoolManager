package me.brandom.schoolmanager.ui.home

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
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.databinding.FragmentHomeBinding
import me.brandom.schoolmanager.ui.MainActivity

@AndroidEntryPoint
class HomeFragment : Fragment(), HomeworkListAdapter.HomeworkManager {
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
        val adapter = HomeworkListAdapter(this)

        binding.apply {
            fragmentHomeRecyclerView.setHasFixedSize(true)
            fragmentHomeRecyclerView.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.retrievalState.collect {
                    fragmentHomeProgressBar.isVisible =
                        it is HomeworkViewModel.HomeworkRetrievalState.Loading
                    fragmentHomeRecyclerView.isVisible =
                        it is HomeworkViewModel.HomeworkRetrievalState.Success && it.homeworkList.isNotEmpty()
                    fragmentHomeNoItemsMessage.isVisible =
                        it is HomeworkViewModel.HomeworkRetrievalState.Success && it.homeworkList.isEmpty()
                    adapter.submitList((it as? HomeworkViewModel.HomeworkRetrievalState.Success)?.homeworkList)
                }
            }

            fragmentHomeAddFab.setOnClickListener {
                viewModel.onAddHomeworkClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeworkEvents.collect {
                when (it) {
                    is HomeworkViewModel.HomeworkEvents.CanEnterForm -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToHomeworkFormFragment("Create homework")
                        findNavController().navigate(action)
                    }
                    is HomeworkViewModel.HomeworkEvents.CannotEnterForm ->
                        Snackbar.make(view, "Pepega", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        setFragmentResultListener("formResult") { _, bundle ->
            when (bundle.getInt("result")) {
                MainActivity.FORM_CREATE_OK_FLAG ->
                    Snackbar.make(view, "Homework created successfully", Snackbar.LENGTH_SHORT)
                        .show()
                MainActivity.FORM_EDIT_OK_FLAG ->
                    Snackbar.make(view, "Homework updated successfully", Snackbar.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun deleteHomework(homework: Homework) {
        viewModel.deleteHomework(homework)
        Snackbar.make(requireView(), "Homework deleted successfully", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                viewModel.onUndoHomeworkClick(homework)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}