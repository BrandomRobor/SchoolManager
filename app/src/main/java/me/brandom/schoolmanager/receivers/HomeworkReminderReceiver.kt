package me.brandom.schoolmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.utils.ApplicationScope
import me.brandom.schoolmanager.utils.NotificationChannelIds
import javax.inject.Inject

@AndroidEntryPoint
class HomeworkReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var subjectDao: SubjectDao

    @Inject
    lateinit var homeworkDao: HomeworkDao

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val async = goAsync()

        applicationScope.launch {
            val homework = homeworkDao.getHomeworkById(id).first()
            val subject = subjectDao.getSubjectById(homework.subjectId).first()

            val notification =
                NotificationCompat.Builder(context, NotificationChannelIds.HOMEWORK_REMINDER)
                    .setContentTitle(homework.hwName)
                    .setContentText(subject.name)
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

            NotificationManagerCompat.from(context).notify(homework.hwId, notification)
            async.finish()
        }
    }
}