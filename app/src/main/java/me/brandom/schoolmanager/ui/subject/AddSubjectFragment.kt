package me.brandom.schoolmanager.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.databinding.FragmentAddSubjectBinding

class AddSubjectFragment : Fragment() {
    private var _binding: FragmentAddSubjectBinding? = null
    private val binding get() = _binding!!

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