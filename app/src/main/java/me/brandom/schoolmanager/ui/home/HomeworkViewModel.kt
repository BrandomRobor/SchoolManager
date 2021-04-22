package me.brandom.schoolmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.entities.SubjectWithHomeworks
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class HomeworkViewModel @Inject constructor(private val homeworkDao: HomeworkDao) : ViewModel() {
    private val _retrievalState =
        MutableStateFlow<HomeworkRetrievalState>(HomeworkRetrievalState.Loading)
    val retrievalState: StateFlow<HomeworkRetrievalState> = _retrievalState

    init {
        viewModelScope.launch {
            homeworkDao.getAllSubjectsWithHomework().collect {
                _retrievalState.value =
                    HomeworkRetrievalState.Success(it, homeworkDao.getHomeworkCount() > 0)
            }
        }
    }

    sealed class HomeworkRetrievalState {
        object Loading : HomeworkRetrievalState()
        data class Success(
            val homeworkList: List<SubjectWithHomeworks>,
            val homeworkExist: Boolean
        ) :
            HomeworkRetrievalState()
    }
}