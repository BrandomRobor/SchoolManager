package me.brandom.schoolmanager.ui.homeworkform

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.FragmentHomeworkFormBinding
import java.text.DateFormat
import java.util.GregorianCalendar

@AndroidEntryPoint
class HomeworkFormFragment : Fragment() {
    private var _binding: FragmentHomeworkFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeworkFormViewModel>()

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

        val defaultTime = GregorianCalendar()
        val selectedDeadline = GregorianCalendar()
        selectedDeadline.set(GregorianCalendar.SECOND, 0)
        selectedDeadline.set(GregorianCalendar.MILLISECOND, 0)

        val adapter =
            ArrayAdapter<Subject>(requireContext(), R.layout.support_simple_spinner_dropdown_item)

        binding.apply {
            fragmentAddHomeworkNameInput.editText?.addTextChangedListener {
                viewModel.homeworkName = it.toString()
            }

            fragmentAddHomeworkDescriptionInput.editText?.addTextChangedListener {
                viewModel.homeworkDescription = it.toString()
            }

            fragmentAddHomeworkDateInput.editText?.let {
                it.setOnClickListener { _ ->
                    DatePickerDialog(
                        requireContext(),
                        { _, year, month, dayOfMonth ->
                            selectedDeadline.set(year, month, dayOfMonth)
                            it.setText(
                                DateFormat.getDateInstance(DateFormat.SHORT)
                                    .format(selectedDeadline.time)
                            )
                        },
                        defaultTime.get(GregorianCalendar.YEAR),
                        defaultTime.get(GregorianCalendar.MONTH),
                        defaultTime.get(GregorianCalendar.DAY_OF_MONTH)
                    ).show()
                }
                it.addTextChangedListener { text ->
                    viewModel.filledDate = text!!.isNotEmpty()
                }
            }

            fragmentAddHomeworkTimeInput.editText?.let {
                it.setOnClickListener { _ ->
                    TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            selectedDeadline.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
                            selectedDeadline.set(GregorianCalendar.MINUTE, minute)
                            it.setText(
                                DateFormat.getTimeInstance(DateFormat.SHORT)
                                    .format(selectedDeadline.time)
                            )
                        },
                        12,
                        0,
                        android.text.format.DateFormat.is24HourFormat(requireContext())
                    ).show()
                }
                it.addTextChangedListener { text ->
                    viewModel.filledTime = text!!.isNotEmpty()
                }
            }

            val autoCompleteTextView =
                fragmentAddHomeworkSubjectInput.editText as MaterialAutoCompleteTextView
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    viewModel.homeworkSubjectId = adapter.getItem(position)?.id!!
                }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                val list = viewModel.subjectList.first()
                adapter.addAll(list)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    autoCompleteTextView.setText(list[0].toString(), false)
                } else {
                    autoCompleteTextView.setText(list[0].toString())
                }
                viewModel.homeworkSubjectId = list[0].id
            }

            fragmentAddHomeworkDoneFab.setOnClickListener {
                viewModel.homeworkDeadline = selectedDeadline.timeInMillis
                viewModel.onSavedClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeworkFormEvents.collect { event ->
                when (event) {
                    is HomeworkFormViewModel.HomeworkFormEvents.InvalidInput -> {
                        Snackbar.make(view, "Pepega", Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeworkFormViewModel.HomeworkFormEvents.ValidInput -> {
                        setFragmentResult("formResult", bundleOf("result" to event.code))
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