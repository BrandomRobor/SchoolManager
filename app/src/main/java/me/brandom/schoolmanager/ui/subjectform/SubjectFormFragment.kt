package me.brandom.schoolmanager.ui.subjectform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.databinding.FragmentSubjectFormBinding

@AndroidEntryPoint
class SubjectFormFragment : Fragment() {
    private var _binding: FragmentSubjectFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SubjectFormViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fragmentAddSubjectNameInput.editText?.addTextChangedListener {
                viewModel.subjectName = it.toString()
            }

            fragmentAddSubjectLocationInput.editText?.addTextChangedListener {
                viewModel.subjectLocation = it.toString()
            }

            fragmentAddSubjectTeacherInput.editText?.addTextChangedListener {
                viewModel.subjectTeacher = it.toString()
            }

            fragmentAddSubjectFab.setOnClickListener {
                viewModel.onSavedClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.subjectFormEvents.collect {
                when (it) {
                    is SubjectFormViewModel.SubjectFormEvents.InvalidInput ->
                        Snackbar.make(view, "Pepega", Snackbar.LENGTH_SHORT).show()
                    is SubjectFormViewModel.SubjectFormEvents.ValidInput -> {
                        setFragmentResult("subjectFormResult", bundleOf("result" to it.code))
                        findNavController().popBackStack()
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