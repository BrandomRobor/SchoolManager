package me.brandom.schoolmanager.ui.dialogs

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.receivers.HomeworkReminderReceiver
import me.brandom.schoolmanager.utils.ApplicationScope
import javax.inject.Inject

@HiltViewModel
class DeleteSubjectViewModel @Inject constructor(
    private val app: Application,
    private val subjectDao: SubjectDao,
    private val homeworkDao: HomeworkDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : AndroidViewModel(app) {
    fun onConfirmDeleteClick(id: Int) = applicationScope.launch {
        val alarmManager = ContextCompat.getSystemService(app, AlarmManager::class.java)!!
        homeworkDao.getAllHomeworkIdsWithSubjectId(id).forEach {
            val intent = Intent(app.applicationContext, HomeworkReminderReceiver::class.java)
            intent.putExtra("id", it)

            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    app.applicationContext,
                    it,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT else PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
        }

        subjectDao.deleteSubjectById(id)
    }
}