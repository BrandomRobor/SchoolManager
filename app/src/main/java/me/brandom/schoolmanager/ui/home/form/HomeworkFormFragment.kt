package me.brandom.schoolmanager.ui.home.form

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.FragmentHomeworkFormBinding
import me.brandom.schoolmanager.ui.home.HomeworkSharedViewModel
import java.text.DateFormat
import java.util.*

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeworkFormFragment : Fragment() {
    private var _binding: FragmentHomeworkFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeworkSharedViewModel by activityViewModels()
    private var subjectListJob: Job? = null
    private var homeworkFormEventsJob: Job? = null

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
        _binding = FragmentHomeworkFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(binding.root, "fab_to_form_transition")

        val homeworkDeadline = GregorianCalendar()
        homeworkDeadline.timeInMillis = viewModel.homeworkDeadline
        homeworkDeadline.set(GregorianCalendar.SECOND, 0)
        homeworkDeadline.set(GregorianCalendar.MILLISECOND, 0)

        val adapter =
            ArrayAdapter<Subject>(requireContext(), R.layout.support_simple_spinner_dropdown_item)

        binding.apply {
            fragmentAddHomeworkNameInput.editText?.setText(viewModel.homeworkName)
            fragmentAddHomeworkDescriptionInput.editText?.setText(viewModel.homeworkDescription)
            fragmentAddHomeworkDateInput.editText?.setText(viewModel.filledDate)
            fragmentAddHomeworkTimeInput.editText?.setText(viewModel.filledTime)

            fragmentAddHomeworkNameInput.editText?.addTextChangedListener {
                viewModel.homeworkName = it.toString()
            }

            fragmentAddHomeworkDescriptionInput.editText?.addTextChangedListener {
                viewModel.homeworkDescription = it.toString()
            }

            fragmentAddHomeworkDateInput.editText?.let {
                it.setOnClickListener { _ ->
                    val picker = MaterialDatePicker.Builder.datePicker()
                        .setSelection(homeworkDeadline.timeInMillis)
                        .setTitleText(R.string.title_deadline_date_picker)
                        .build()
                    picker.addOnPositiveButtonClickListener { selection ->
                        val selectedDate = GregorianCalendar.getInstance()
                        selectedDate.timeInMillis =
                            selection - Calendar.getInstance().timeZone.getOffset(selection)
                        homeworkDeadline.set(
                            selectedDate.get(GregorianCalendar.YEAR),
                            selectedDate.get(GregorianCalendar.MONTH),
                            selectedDate.get(GregorianCalendar.DAY_OF_MONTH)
                        )
                        it.setText(
                            DateFormat.getDateInstance(DateFormat.SHORT)
                                .format(homeworkDeadline.timeInMillis)
                        )
                    }
                    picker.show(parentFragmentManager, picker.toString())
                }
                it.addTextChangedListener { text ->
                    viewModel.filledDate = text.toString()
                }
            }

            fragmentAddHomeworkTimeInput.editText?.let {
                it.setOnClickListener { _ ->
                    val timeNotSet = viewModel.filledTime.isEmpty()
                    val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(
                            if (android.text.format.DateFormat.is24HourFormat(
                                    requireContext()
                                )
                            ) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                        )
                        .setHour(if (timeNotSet) 12 else homeworkDeadline.get(GregorianCalendar.HOUR_OF_DAY))
                        .setMinute(if (timeNotSet) 0 else homeworkDeadline.get(GregorianCalendar.MINUTE))
                        .setTitleText(R.string.title_deadline_time_picker)
                        .build()
                    picker.addOnPositiveButtonClickListener { _ ->
                        homeworkDeadline.set(GregorianCalendar.HOUR_OF_DAY, picker.hour)
                        homeworkDeadline.set(GregorianCalendar.MINUTE, picker.minute)
                        it.setText(
                            DateFormat.getTimeInstance(DateFormat.SHORT)
                                .format(homeworkDeadline.time)
                        )
                    }
                    picker.show(parentFragmentManager, picker.toString())
                }
                it.addTextChangedListener { text ->
                    viewModel.filledTime = text.toString()
                }
            }

            val autoCompleteTextView =
                fragmentAddHomeworkSubjectInput.editText as MaterialAutoCompleteTextView
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    viewModel.homeworkSubjectId = adapter.getItem(position)?.id!!
                }

            subjectListJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.subjectList.collect {
                    adapter.addAll(it)
                    val subject = it.find { s -> s.id == viewModel.homework?.subjectId } ?: it[0]
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        autoCompleteTextView.setText(subject.name, false)
                    } else {
                        autoCompleteTextView.setText(subject.name)
                    }
                    viewModel.homeworkSubjectId = subject.id
                }
            }

            fragmentAddHomeworkDoneFab.setOnClickListener {
                viewModel.homeworkDeadline = homeworkDeadline.timeInMillis
                viewModel.onSavedClick()
            }
        }

        homeworkFormEventsJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeworkFormEvents.collect { event ->
                when (event) {
                    is HomeworkSharedViewModel.HomeworkFormEvents.InvalidInput -> {
                        Snackbar.make(view, R.string.error_missing_required, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    is HomeworkSharedViewModel.HomeworkFormEvents.ValidInput -> {
                        setFragmentResult(
                            "formResult",
                            bundleOf(
                                "result" to event.code,
                                "id" to event.homework.hwId,
                                "deadline" to event.homework.deadline
                            )
                        )
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeworkFormEventsJob?.cancel()
        subjectListJob?.cancel()
        _binding = null
    }
}