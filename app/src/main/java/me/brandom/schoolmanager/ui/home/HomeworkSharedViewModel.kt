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
    var homework: Homework? = null

    var homeworkName = ""
        get() = state.get<String>("homeworkName") ?: homework?.hwName ?: field
        set(value) = state.set("homeworkName", value)
    var homeworkDescription = ""
        get() = state.get<String>("homeworkDescription") ?: homework?.description ?: field
        set(value) = state.set("homeworkDescription", value)
    var filledDate = ""
        get() = state.get<String>("filledDate") ?: homework?.formattedDate ?: field
        set(value) = state.set("filledDate", value)
    var filledTime = ""
        get() = state.get<String>("filledTime") ?: homework?.formattedTime ?: field
        set(value) = state.set("filledTime", value)
    var homeworkDeadline = System.currentTimeMillis()
        get() = state.get<Long>("homeworkDeadline") ?: homework?.deadline ?: field
        set(value) = state.set("homeworkDeadline", value)
    var homeworkSubjectId = 0
        get() = state.get<Int>("homeworkSubjectId") ?: homework?.subjectId ?: field
        set(value) = state.set("homeworkSubjectId", value)

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

        if (filledDate.isEmpty() || filledTime.isEmpty()) {
            sendInvalidInputEvent()
            return
        }

        if (homeworkSubjectId == 0) {
            sendInvalidInputEvent()
            return
        }

        val clearDescription =
            if (homeworkDescription.isBlank()) null else homeworkDescription

        homework?.also {
            val updatedHomework = it.copy(
                hwName = homeworkName,
                deadline = homeworkDeadline,
                subjectId = homeworkSubjectId,
                description = clearDescription
            )
            updateHomework(updatedHomework)
            sendValidInputEvent(MainActivity.FORM_EDIT_OK_FLAG, updatedHomework)
        } ?: run {
            val newHomework =
                Homework(homeworkName, homeworkDeadline, homeworkSubjectId, clearDescription)
            createHomework(newHomework)
        }
    }

    fun resetStates(homework: Homework? = null) {
        clearSavedState()
        this.homework = homework
    }

    private fun sendInvalidInputEvent() = viewModelScope.launch {
        homeworkFormEventsChannel.send(HomeworkFormEvents.InvalidInput)
    }

    private fun sendValidInputEvent(code: Int, homework: Homework) =
        viewModelScope.launch {
            homeworkFormEventsChannel.send(HomeworkFormEvents.ValidInput(code, homework))
        }

    private fun createHomework(homework: Homework) = viewModelScope.launch {
        sendValidInputEvent(
            MainActivity.FORM_CREATE_OK_FLAG,
            homework.copy(hwId = homeworkDao.insertHomework(homework).toInt())
        )
    }

    private fun updateHomework(homework: Homework) = viewModelScope.launch {
        homeworkDao.updateHomework(homework)
    }

    // Hate this, but this is a downside of having a shared viewmodel
    private fun clearSavedState() {
        state.remove<String>("homeworkName")
        state.remove<String>("homeworkDescription")
        state.remove<String>("filledDate")
        state.remove<String>("filledTime")
        state.remove<Long>("homeworkDeadline")
        state.remove<Int>("homeworkSubjectId")
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
        data class ValidInput(val code: Int, val homework: Homework) :
            HomeworkFormEvents()
    }
}