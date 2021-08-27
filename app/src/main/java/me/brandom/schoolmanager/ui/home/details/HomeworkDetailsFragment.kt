package me.brandom.schoolmanager.ui.home.details

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.databinding.FragmentHomeworkDetailsBinding
import me.brandom.schoolmanager.ui.home.HomeworkSharedViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeworkDetailsFragment : Fragment() {
    private var _binding: FragmentHomeworkDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeworkSharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            val ty = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.colorSurface, ty, true)

            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(ty.data)
        }
    }

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
        setHasOptionsMenu(true)
        ViewCompat.setTransitionName(binding.root, "homework_to_details_transition")

        binding.apply {
            homeworkDetailsName.text = viewModel.homeworkName
            homeworkDetailsDescription.isVisible = viewModel.homeworkDescription.isNotEmpty()
            homeworkDetailsDescription.text = viewModel.homeworkDescription
            homeworkDetailsDeadline.text = viewModel.homework!!.formattedDateTime
            homeworkDetailsSubject.text = viewModel.subject.name
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.homework_menu, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}