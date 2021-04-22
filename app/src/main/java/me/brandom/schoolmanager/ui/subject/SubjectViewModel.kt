package me.brandom.schoolmanager.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Subject
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(private val subjectDao: SubjectDao) : ViewModel() {
    private val _retrievalState =
        MutableStateFlow<SubjectRetrievalState>(SubjectRetrievalState.Loading)
    val retrievalState: StateFlow<SubjectRetrievalState> = _retrievalState

    init {
        viewModelScope.launch {
            subjectDao.getAllSubjects().collect {
                _retrievalState.value = SubjectRetrievalState.Success(it)
            }
        }
    }

    fun onAddSubjectSubmit(subject: Subject) {
        viewModelScope.launch {
            subjectDao.insertSubject(subject)
        }
    }

    sealed class SubjectRetrievalState {
        object Loading : SubjectRetrievalState()
        data class Success(val subjectList: List<Subject>) : SubjectRetrievalState()
    }
}