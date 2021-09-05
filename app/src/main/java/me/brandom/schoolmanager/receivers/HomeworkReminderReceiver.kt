package me.brandom.schoolmanager.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.utils.ApplicationScope
import me.brandom.schoolmanager.utils.Constants
import me.brandom.schoolmanager.utils.NotificationTimeHelper
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
    lateinit var notificationTimeHelper: NotificationTimeHelper

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
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(
                        R.drawable.ic_done, "Mark as complete", PendingIntent.getBroadcast(
                            context,
                            homeworkId,
                            actionIntent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT else PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    )
                    .build()
            )

            val nextReminderTime =
                notificationTimeHelper.getNextNotificationTime(hwWthSubject.homework.deadline)

            if (hwWthSubject.homework.deadline != nextReminderTime) {
                val nextReminderIntent = Intent(context, HomeworkReminderReceiver::class.java)
                nextReminderIntent.putExtra("id", homeworkId)

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    ContextCompat.getSystemService(context, AlarmManager::class.java)!!,
                    AlarmManager.RTC_WAKEUP,
                    nextReminderTime,
                    PendingIntent.getBroadcast(
                        context,
                        homeworkId,
                        nextReminderIntent,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT else PendingIntent.FLAG_CANCEL_CURRENT
                    )
                )
            }
        }
    }
}