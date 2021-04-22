package me.brandom.schoolmanager.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.FragmentAddSubjectBinding

@AndroidEntryPoint
class AddSubjectFragment : Fragment() {
    private var _binding: FragmentAddSubjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SubjectViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fragmentAddSubjectNameInput.editText!!.addTextChangedListener {
                fragmentAddSubjectNameInput.error = null
            }

            fragmentAddSubjectFab.setOnClickListener {
                if (checkRequiredFields()) {
                    viewModel.onAddSubjectSubmit(
                        Subject(
                            fragmentAddSubjectNameInput.editText!!.text.toString(),
                            if (fragmentAddSubjectLocationInput.editText!!.text.isBlank()) null else fragmentAddSubjectLocationInput.editText!!.text.toString(),
                            if (fragmentAddSubjectTeacherInput.editText!!.text.isBlank()) null else fragmentAddSubjectTeacherInput.editText!!.text.toString()
                        )
                    )
                    Toast.makeText(requireContext(), "Subject added", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkRequiredFields(): Boolean {
        var validInput = true

        binding.apply {
            if (fragmentAddSubjectNameInput.editText!!.text.isBlank()) {
                fragmentAddSubjectNameInput.error =
                    getString(R.string.fragment_add_homework_required)
                validInput = false
            }
        }

        return validInput
    }
}