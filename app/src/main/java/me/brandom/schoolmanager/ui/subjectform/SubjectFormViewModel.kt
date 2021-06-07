package me.brandom.schoolmanager.ui.subjectform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.ui.MainActivity
import javax.inject.Inject

@HiltViewModel
class SubjectFormViewModel @Inject constructor(
    private val subjectDao: SubjectDao,
    private val state: SavedStateHandle
) : ViewModel() {
    val subject = state.get<Subject>("subject")
    var subjectName = state.get<String>("subjectName").orEmpty()
        set(value) {
            field = value
            state.set("subjectName", value)
        }
    var subjectLocation = state.get<String>("subjectLocation")
        set(value) {
            val cleaned = if (value.isNullOrBlank()) null else value
            field = cleaned
            state.set("subjectLocation", cleaned)
        }
    var subjectTeacher = state.get<String>("subjectTeacher")
        set(value) {
            val cleaned = if (value.isNullOrBlank()) null else value
            field = cleaned
            state.set("subjectTeacher", cleaned)
        }

    private val subjectFormEventsChannel = Channel<SubjectFormEvents>()
    val subjectFormEvents = subjectFormEventsChannel.receiveAsFlow()

    fun onSavedClick() {
        if (subjectName.isBlank()) {
            sendInvalidInputEvent()
            return
        }

        if (subject == null) {
            val newSubject = Subject(subjectName, subjectLocation, subjectTeacher)
            createSubject(newSubject)
            sendValidInputEvent(MainActivity.FORM_CREATE_OK_FLAG)
        } else {
            updateSubject(
                subject.copy(
                    name = subjectName,
                    location = subjectLocation,
                    teacherName = subjectTeacher
                )
            )
            sendValidInputEvent(MainActivity.FORM_EDIT_OK_FLAG)
        }
    }

    private fun createSubject(subject: Subject) = viewModelScope.launch {
        subjectDao.insertSubject(subject)
    }

    private fun updateSubject(subject: Subject) = viewModelScope.launch {
        subjectDao.updateSubject(subject)
    }

    private fun sendInvalidInputEvent() = viewModelScope.launch {
        subjectFormEventsChannel.send(SubjectFormEvents.InvalidInput)
    }

    private fun sendValidInputEvent(code: Int) = viewModelScope.launch {
        subjectFormEventsChannel.send(SubjectFormEvents.ValidInput(code))
    }

    sealed class SubjectFormEvents {
        object InvalidInput : SubjectFormEvents()
        data class ValidInput(val code: Int) : SubjectFormEvents()
    }
}