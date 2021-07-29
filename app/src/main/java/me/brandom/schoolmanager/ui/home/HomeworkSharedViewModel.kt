package me.brandom.schoolmanager.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.ui.MainActivity
import me.brandom.schoolmanager.utils.SortOrder
import javax.inject.Inject

@ExperimentalCoroutinesApi
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

    private val sortOrderFlow = MutableStateFlow(SortOrder.BY_NAME)
    val currentSortOrder = sortOrderFlow.value

    private val homeworkListCustomized = sortOrderFlow.flatMapLatest {
        homeworkDao.getAllHomeworkWithSubject(it)
    }
    val homeworkList = homeworkListCustomized.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    private val homeworkEventsChannel = Channel<HomeworkFormChecks>()
    val homeworkEvents = homeworkEventsChannel.receiveAsFlow()

    private val homeworkFormEventsChannel = Channel<HomeworkFormEvents>()
    val homeworkFormEvents = homeworkFormEventsChannel.receiveAsFlow()

    val subjectList =
        subjectDao.getAllSubjects().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
            homeworkEventsChannel.send(HomeworkFormChecks.OK)
        } else {
            homeworkEventsChannel.send(HomeworkFormChecks.NO_SUBJECTS)
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

    fun setSortOrder(sortOrder: SortOrder) = viewModelScope.launch {
        sortOrderFlow.emit(sortOrder)
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

    fun updateHomework(homework: Homework) = viewModelScope.launch {
        homeworkDao.updateHomework(homework)
    }

    // ¯\_(ツ)_/¯ This is ugly, but it works. Disadvantages of using a shared viewmodel
    private fun clearSavedState() {
        state.keys().forEach {
            state.remove<Any>(it)
        }
    }

    enum class HomeworkFormChecks {
        OK,
        NO_SUBJECTS
    }

    sealed class HomeworkFormEvents {
        object InvalidInput : HomeworkFormEvents()
        data class ValidInput(val code: Int, val homework: Homework) :
            HomeworkFormEvents()
    }
}