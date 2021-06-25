package me.brandom.schoolmanager.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import me.brandom.schoolmanager.ui.MainActivity
import javax.inject.Inject

@HiltViewModel
class HomeworkSharedViewModel @Inject constructor(
    private val homeworkDao: HomeworkDao,
    private val subjectDao: SubjectDao,
    private val state: SavedStateHandle
) : ViewModel() {
    val homework = state.get<Homework>("homework")
    var homeworkName = state.get<String>("homeworkName") ?: homework?.hwName.orEmpty()
        set(value) {
            field = value
            state.set("homeworkName", value)
        }
    var homeworkDescription = state.get<String>("homeworkDescription")
        set(value) {
            field = value
            state.set("homeworkDescription", value)
        }
    var filledDate = state.get<Boolean>("filledDate") ?: false
        set(value) {
            field = value
            state.set("filledDate", value)
        }
    var filledTime = state.get<Boolean>("filledTime") ?: false
        set(value) {
            field = value
            state.set("filledTime", value)
        }
    var homeworkDeadline = state.get<Long>("homeworkDeadline") ?: System.currentTimeMillis()
        set(value) {
            field = value
            state.set("homeworkDeadline", value)
        }
    var homeworkSubjectId = state.get<Int>("homeworkSubjectId") ?: 0
        set(value) {
            field = value
            state.set("homeworkSubjectId", value)
        }

    private val _retrievalState =
        MutableStateFlow<HomeworkRetrievalState>(HomeworkRetrievalState.Loading)
    val retrievalState: StateFlow<HomeworkRetrievalState> = _retrievalState

    private val homeworkEventsChannel = Channel<HomeworkEvents>()
    val homeworkEvents = homeworkEventsChannel.receiveAsFlow()

    private val homeworkFormEventsChannel = Channel<HomeworkFormEvents>()
    val homeworkFormEvents = homeworkFormEventsChannel.receiveAsFlow()

    val subjectList =
        subjectDao.getAllSubjects().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            homeworkDao.getAllHomeworkWithSubject().collect {
                _retrievalState.value = HomeworkRetrievalState.Success(it)
            }
        }
    }

    fun onUndoHomeworkClick(homework: Homework) = viewModelScope.launch {
        homeworkDao.insertHomework(homework)
    }

    fun deleteHomework(homework: Homework) {
        viewModelScope.launch {
            homeworkDao.deleteHomework(homework)
        }
    }

    fun onAddHomeworkClick() = viewModelScope.launch {
        if (subjectDao.getSubjectCount().first() > 0) {
            homeworkEventsChannel.send(HomeworkEvents.CanEnterForm)
        } else {
            homeworkEventsChannel.send(HomeworkEvents.CannotEnterForm)
        }
    }

    fun onSavedClick() {
        if (homeworkName.isBlank()) {
            sendInvalidInputEvent()
            return
        }

        if (!filledDate || !filledTime) {
            sendInvalidInputEvent()
            return
        }

        if (homeworkSubjectId == 0) {
            sendInvalidInputEvent()
            return
        }

        val clearDescription =
            if (homeworkDescription.isNullOrBlank()) null else homeworkDescription

        if (homework == null) {
            val newHomework =
                Homework(homeworkName, homeworkDeadline, homeworkSubjectId, clearDescription)
            createHomework(newHomework)
            sendValidInputEvent(HomeworkFormEvents.ValidInput(MainActivity.FORM_CREATE_OK_FLAG))
        } else {
            val updatedHomework = homework.copy(
                hwName = homeworkName,
                deadline = homeworkDeadline,
                subjectId = homeworkSubjectId,
                description = clearDescription
            )
            updateHomework(updatedHomework)
            sendValidInputEvent(HomeworkFormEvents.ValidInput(MainActivity.FORM_EDIT_OK_FLAG))
        }
    }

    private fun sendInvalidInputEvent() = viewModelScope.launch {
        homeworkFormEventsChannel.send(HomeworkFormEvents.InvalidInput)
    }

    private fun sendValidInputEvent(event: HomeworkFormEvents.ValidInput) = viewModelScope.launch {
        homeworkFormEventsChannel.send(event)
    }

    private fun createHomework(homework: Homework) = viewModelScope.launch {
        homeworkDao.insertHomework(homework)
    }

    private fun updateHomework(homework: Homework) = viewModelScope.launch {
        homeworkDao.updateHomework(homework)
    }

    sealed class HomeworkRetrievalState {
        object Loading : HomeworkRetrievalState()
        data class Success(val homeworkList: List<HomeworkWithSubject>) : HomeworkRetrievalState()
    }

    sealed class HomeworkEvents {
        object CanEnterForm : HomeworkEvents()
        object CannotEnterForm : HomeworkEvents()
    }

    sealed class HomeworkFormEvents {
        object InvalidInput : HomeworkFormEvents()
        data class ValidInput(val code: Int) : HomeworkFormEvents()
    }
}