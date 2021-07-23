package me.brandom.schoolmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.utils.ApplicationScope
import me.brandom.schoolmanager.utils.Constants
import me.brandom.schoolmanager.utils.goAsync
import javax.inject.Inject

@AndroidEntryPoint
class HomeworkReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var homeworkDao: HomeworkDao

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        goAsync(applicationScope) {
            val hwWthSubject =
                homeworkDao.getHomeworkWithSubjectByHwId(intent.getIntExtra("id", 0))

            NotificationManagerCompat.from(context).notify(
                hwWthSubject.homework.hwId,
                NotificationCompat.Builder(context, Constants.HOMEWORK_REMINDER_CHANNEL_ID)
                    .setContentTitle(hwWthSubject.homework.hwName)
                    .setContentText(hwWthSubject.subject.name)
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            )
        }
    }
}