package me.brandom.schoolmanager.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.databinding.FragmentAddHomeworkBinding

class AddHomeworkFragment : Fragment() {
    private var _binding: FragmentAddHomeworkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            fragmentAddHomeworkDoneFab.setOnClickListener {
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
            if (fragmentAddHomeworkNameInput.editText!!.text.isBlank()) {
                fragmentAddHomeworkNameInput.error =
                    getString(R.string.fragment_add_homework_required)
                validInput = false
            }

            if (fragmentAddHomeworkDateInput.editText!!.text.isEmpty()) {
                fragmentAddHomeworkDateInput.error =
                    getString(R.string.fragment_add_homework_required)
                validInput = false
            }

            if (fragmentAddHomeworkTimeInput.editText!!.text.isEmpty()) {
                fragmentAddHomeworkTimeInput.error =
                    getString(R.string.fragment_add_homework_required)
                validInput = false
            }

            return validInput
        }
    }
}