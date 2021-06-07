package me.brandom.schoolmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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

    init {
        viewModelScope.launch {
            homeworkDao.getAllHomeworkWithSubject().collect {
                _retrievalState.value =
                    HomeworkRetrievalState.Success(it, homeworkDao.getHomeworkCount() > 0)
            }
        }
    }

    fun getSubjectCount() =
        subjectDao.getSubjectCount()

    fun onUndoHomeworkClick(homework: Homework) = viewModelScope.launch {
        homeworkDao.insertHomework(homework)
    }

    fun deleteHomework(homework: Homework) {
        viewModelScope.launch {
            homeworkDao.deleteHomework(homework)
        }
    }

    sealed class HomeworkRetrievalState {
        object Loading : HomeworkRetrievalState()
        data class Success(
            val homeworkList: List<HomeworkWithSubject>,
            val homeworkExist: Boolean
        ) :
            HomeworkRetrievalState()
    }
}