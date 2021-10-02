package me.brandom.schoolmanager.receivers

import android.app.PendingIntent
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
import me.brandom.schoolmanager.utils.ReminderHelper
import me.brandom.schoolmanager.utils.goAsync
import javax.inject.Inject

@AndroidEntryPoint
class HomeworkReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var homeworkDao: HomeworkDao

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var reminderHelper: ReminderHelper

    override fun onReceive(context: Context, intent: Intent) {
        goAsync(applicationScope) {
            val homeworkId = intent.getIntExtra("id", 0)
            val actionIntent = Intent(context, CompleteActionReceiver::class.java)
            actionIntent.putExtra("id", homeworkId)

            val hwWthSubject =
                homeworkDao.getHomeworkWithSubjectByHwId(homeworkId)

            NotificationManagerCompat.from(context).notify(
                hwWthSubject.homework.hwId,
                NotificationCompat.Builder(context, Constants.HOMEWORK_REMINDER_CHANNEL_ID)
                    .setContentTitle(hwWthSubject.homework.hwName)
                    .setContentText(hwWthSubject.subject.name)
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .addAction(
                        R.drawable.ic_done,
                        context.resources.getString(R.string.action_mark_complete),
                        PendingIntent.getBroadcast(
                            context,
                            homeworkId,
                            actionIntent,
                            reminderHelper.pendingIntentFlags
                        )
                    )
                    .build()
            )

            if (hwWthSubject.homework.deadline != reminderHelper.getNextReminderTime(hwWthSubject.homework.deadline)) {
                reminderHelper.createReminderAlarm(homeworkId, hwWthSubject.homework.deadline)
            }
        }
    }
}