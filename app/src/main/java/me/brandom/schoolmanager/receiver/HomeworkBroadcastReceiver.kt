package me.brandom.schoolmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import javax.inject.Inject

@AndroidEntryPoint
class HomeworkBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var homeworkDao: HomeworkDao

    @Inject
    lateinit var subjectDao: SubjectDao

    companion object {
        const val HOMEWORK_REMINDERS_CHANNEL_ID =
            "me.brandom.schoolmanager.HOMEWORK_REMINDERS_CHANNEL"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val homeworkId = intent!!.getIntExtra("id", 0)

        GlobalScope.launch {
            homeworkDao.getHomeworkById(homeworkId).collect { homework ->
                context?.let {
                    NotificationManagerCompat.from(it).notify(
                        homework.hwId, NotificationCompat.Builder(it, HOMEWORK_REMINDERS_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_book)
                            .setContentTitle(homework.hwName)
                            .setContentText(
                                subjectDao.getSubjectById(homework.subjectId).first().name
                            )
                            .build()
                    )
                }
            }
        }
    }
}