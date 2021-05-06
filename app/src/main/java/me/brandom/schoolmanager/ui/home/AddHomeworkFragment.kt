package me.brandom.schoolmanager.ui.home

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.FragmentAddHomeworkBinding
import me.brandom.schoolmanager.receiver.HomeworkBroadcastReceiver
import me.brandom.schoolmanager.ui.MainActivity
import java.util.GregorianCalendar

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AddHomeworkFragment : Fragment() {
    private var _binding: FragmentAddHomeworkBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeworkViewModel>()

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

        val today = GregorianCalendar()
        val deadlineDateTime = GregorianCalendar()
        var selectedSubject: Subject? = null

        binding.apply {
            fragmentAddHomeworkNameInput.editText!!.addTextChangedListener {
                fragmentAddHomeworkNameInput.error = null
            }

            fragmentAddHomeworkDateInput.editText?.let {
                it.setOnClickListener { _ ->
                    DatePickerDialog(
                        requireContext(),
                        { _, year, month, dayOfMonth ->
                            deadlineDateTime.set(year, month, dayOfMonth)
                            fragmentAddHomeworkDateInput.error = null
                            it.setText(
                                DateFormat.getDateFormat(requireContext())
                                    .format(deadlineDateTime.time)
                            )
                        },
                        today.get(GregorianCalendar.YEAR),
                        today.get(GregorianCalendar.MONTH),
                        today.get(GregorianCalendar.DAY_OF_MONTH)
                    ).show()
                }
            }

            fragmentAddHomeworkTimeInput.editText?.let {
                it.setOnClickListener { _ ->
                    TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            deadlineDateTime.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
                            deadlineDateTime.set(GregorianCalendar.MINUTE, minute)
                            fragmentAddHomeworkTimeInput.error = null
                            it.setText(
                                DateFormat.getTimeFormat(requireContext())
                                    .format(deadlineDateTime.time)
                            )
                        },
                        today.get(GregorianCalendar.HOUR_OF_DAY),
                        today.get(GregorianCalendar.MINUTE),
                        DateFormat.is24HourFormat(requireContext())
                    ).show()
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.getSpinnerSubjectList().collect {
                    (fragmentAddHomeworkSubjectInput.editText as MaterialAutoCompleteTextView).apply {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.support_simple_spinner_dropdown_item,
                            it
                        )
                        setAdapter(adapter)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            setText(it[0].toString(), false)
                        } else {
                            setText(it[0].toString())
                        }
                        selectedSubject = adapter.getItem(0)
                        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            selectedSubject = adapter.getItem(position)
                        }
                    }
                }
            }

            fragmentAddHomeworkDoneFab.setOnClickListener {
                if (checkRequiredFields()) {
                    val newHomework = Homework(
                        fragmentAddHomeworkNameInput.editText!!.text.toString(),
                        deadlineDateTime.timeInMillis,
                        selectedSubject!!.id,
                        if (fragmentAddHomeworkDescriptionInput.editText!!.text.isBlank()) null else fragmentAddHomeworkDescriptionInput.editText!!.text.toString()
                    )
                    viewModel.onAddHomeworkSubmit(newHomework)
                    createBroadcastAlarm(newHomework, selectedSubject!!.name)
                    Toast.makeText(
                        requireContext(),
                        "Homework added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as MainActivity).closeKeyboard()
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

    private fun createBroadcastAlarm(homework: Homework, subjectName: String) {
        val intent = Intent(requireContext(), HomeworkBroadcastReceiver::class.java)
        val bundle = Bundle()

        bundle.putParcelable("homework", homework)
        bundle.putString("name", subjectName)

        intent.putExtra("bundle", bundle)

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)!!,
            AlarmManager.RTC_WAKEUP,
            homework.deadline,
            PendingIntent.getBroadcast(requireContext(), 0, intent, 0)
        )
    }
}