package me.brandom.schoolmanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp
import me.brandom.schoolmanager.internal.receiver.HomeworkBroadcastReceiver

@HiltAndroidApp
class SchoolManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(
                NotificationChannel(
                    HomeworkBroadcastReceiver.HOMEWORK_REMINDERS_CHANNEL_ID,
                    getString(R.string.homework_reminders_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}