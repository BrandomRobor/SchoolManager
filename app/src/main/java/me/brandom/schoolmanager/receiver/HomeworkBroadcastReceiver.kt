package me.brandom.schoolmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Homework

class HomeworkBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val HOMEWORK_REMINDERS_CHANNEL_ID =
            "me.brandom.schoolmanager.HOMEWORK_REMINDERS_CHANNEL"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent!!.getBundleExtra("bundle")!!
        val homework = bundle.getParcelable<Homework>("homework")!!

        context?.let {
            NotificationManagerCompat.from(it).notify(
                homework.hwId, NotificationCompat.Builder(it, HOMEWORK_REMINDERS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_book)
                    .setContentTitle(homework.hwName)
                    .setContentText(bundle.getString("name")!!)
                    .build()
            )
        }
    }
}