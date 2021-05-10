package me.brandom.schoolmanager.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class HomeworkViewModel @Inject constructor(
    private val homeworkDao: HomeworkDao,
    private val subjectDao: SubjectDao
) : ViewModel() {
    private val _retrievalState =
        MutableStateFlow<HomeworkRetrievalState>(HomeworkRetrievalState.Loading)
    val retrievalState: StateFlow<HomeworkRetrievalState> = _retrievalState
    private val _newAddedHomeworkId = MutableSharedFlow<Int>()
    val newAddedHomeworkId: SharedFlow<Int> = _newAddedHomeworkId

    init {
        viewModelScope.launch {
            homeworkDao.getAllHomeworkWithSubject().collect {
                _retrievalState.value =
                    HomeworkRetrievalState.Success(it, homeworkDao.getHomeworkCount() > 0)
            }
        }
    }

    fun getSpinnerSubjectList() =
        subjectDao.getAllSubjects()

    fun getSubjectCount() =
        subjectDao.getSubjectCount()

    fun onAddHomeworkSubmit(homework: Homework) {
        viewModelScope.launch {
            val newId = homeworkDao.insertHomework(homework).toInt()
            Log.i("Test", newId.toString())
            _newAddedHomeworkId.emit(newId)
        }
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