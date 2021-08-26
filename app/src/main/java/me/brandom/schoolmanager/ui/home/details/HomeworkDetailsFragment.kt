package me.brandom.schoolmanager.ui.home.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.brandom.schoolmanager.databinding.FragmentHomeworkDetailsBinding
import me.brandom.schoolmanager.ui.home.HomeworkSharedViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeworkDetailsFragment : Fragment() {
    private var _binding: FragmentHomeworkDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeworkSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeworkDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            homeworkDetailsName.text = viewModel.homeworkName
            homeworkDetailsDescription.text = viewModel.homeworkDescription
            homeworkDetailsDeadline.text = viewModel.homework!!.formattedDateTime
            homeworkDetailsSubject.text = viewModel.subject.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}