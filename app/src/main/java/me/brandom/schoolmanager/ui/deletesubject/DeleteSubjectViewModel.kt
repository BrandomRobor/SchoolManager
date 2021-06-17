package me.brandom.schoolmanager.ui.deletesubject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.SubjectDao
import javax.inject.Inject

@HiltViewModel
class DeleteSubjectViewModel @Inject constructor(private val subjectDao: SubjectDao) : ViewModel() {
    fun onConfirmDeleteClick(id: Int) = viewModelScope.launch {
        subjectDao.deleteSubjectById(id)
    }
}