package me.brandom.schoolmanager.ui.subject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.ui.MainActivity
import javax.inject.Inject

@HiltViewModel
class SubjectSharedViewModel @Inject constructor(
    private val subjectDao: SubjectDao,
    private val state: SavedStateHandle
) : ViewModel() {
    var subject: Subject? = null

    var subjectName = ""
        get() = state.get<String>("subjectName") ?: subject?.name ?: field
        set(value) = state.set("subjectName", value)
    var subjectLocation = ""
        get() = state.get<String>("subjectLocation") ?: subject?.location ?: field
        set(value) = state.set("subjectLocation", value)
    var subjectTeacher = ""
        get() = state.get<String>("subjectTeacher") ?: subject?.teacherName ?: field
        set(value) = state.set("subjectTeacher", value)

    private val _retrievalState =
        MutableStateFlow<SubjectRetrievalState>(SubjectRetrievalState.Loading)
    val retrievalState: StateFlow<SubjectRetrievalState> = _retrievalState

    private val subjectFormEventsChannel = Channel<SubjectFormEvents>()
    val subjectFormEvents = subjectFormEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            subjectDao.getAllSubjects().collect {
                _retrievalState.value = SubjectRetrievalState.Success(it)
            }
        }
    }

    fun onSavedClick() {
        if (subjectName.isBlank()) {
            sendInvalidInputEvent()
            return
        }

        subject?.also {
            val updatedSubject = it.copy(
                name = subjectName,
                location = subjectLocation,
                teacherName = subjectTeacher
            )
            updateSubject(updatedSubject)
            sendValidInputEvent(MainActivity.FORM_EDIT_OK_FLAG)
        } ?: run {
            val newSubject = Subject(subjectName, subjectLocation, subjectTeacher)
            createSubject(newSubject)
            sendValidInputEvent(MainActivity.FORM_CREATE_OK_FLAG)
        }
    }

    fun resetState(subject: Subject? = null) {
        this.subject = subject
        clearState()
    }

    private fun clearState() {
        state.keys().forEach {
            state.remove<Any>(it)
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

    sealed class SubjectRetrievalState {
        object Loading : SubjectRetrievalState()
        data class Success(val subjectList: List<Subject>) : SubjectRetrievalState()
    }

    sealed class SubjectFormEvents {
        object InvalidInput : SubjectFormEvents()
        data class ValidInput(val code: Int) : SubjectFormEvents()
    }
}