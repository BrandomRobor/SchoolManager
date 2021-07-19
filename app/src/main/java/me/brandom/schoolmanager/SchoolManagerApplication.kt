package me.brandom.schoolmanager

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp
import me.brandom.schoolmanager.utils.NotificationChannelIds

@HiltAndroidApp
class SchoolManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val reminderChannel = NotificationChannelCompat.Builder(
            NotificationChannelIds.HOMEWORK_REMINDER,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName("Homework reminders")
            .build()

        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(reminderChannel)
    }
}