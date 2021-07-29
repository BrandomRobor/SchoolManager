package me.brandom.schoolmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.utils.ApplicationScope
import me.brandom.schoolmanager.utils.goAsync
import javax.inject.Inject

@AndroidEntryPoint
class CompleteActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var homeworkDao: HomeworkDao

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        goAsync(applicationScope) {
            val homeworkId = intent.getIntExtra("id", 0)
            val homework = homeworkDao.getHomeworkWithSubjectByHwId(homeworkId)
            homeworkDao.updateHomework(homework.homework.copy(isComplete = true))
            NotificationManagerCompat.from(context).cancel(homeworkId)
        }
    }
}