package me.brandom.schoolmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import javax.inject.Inject

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val homeworkDao: HomeworkDao,
    private val subjectDao: SubjectDao
) : ViewModel() {
    private val _retrievalState =
        MutableStateFlow<HomeworkRetrievalState>(HomeworkRetrievalState.Loading)
    val retrievalState: StateFlow<HomeworkRetrievalState> = _retrievalState

    private val homeworkEventsChannel = Channel<HomeworkEvents>()
    val homeworkEvents = homeworkEventsChannel.receiveAsFlow()

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

    sealed class HomeworkRetrievalState {
        object Loading : HomeworkRetrievalState()
        data class Success(val homeworkList: List<HomeworkWithSubject>) : HomeworkRetrievalState()
    }

    sealed class HomeworkEvents {
        object CanEnterForm : HomeworkEvents()
        object CannotEnterForm : HomeworkEvents()
    }
}