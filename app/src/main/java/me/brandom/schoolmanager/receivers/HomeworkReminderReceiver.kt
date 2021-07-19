package me.brandom.schoolmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.utils.NotificationChannelIds

class HomeworkReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.getBundleExtra("bundle")!!
        val homework = bundle.getParcelable<Homework>("homework")!!

        val notification =
            NotificationCompat.Builder(context, NotificationChannelIds.HOMEWORK_REMINDER)
                .setContentTitle(homework.hwName)
                .setSmallIcon(R.drawable.ic_book)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        NotificationManagerCompat.from(context).notify(homework.hwId, notification)
    }
}