package me.brandom.schoolmanager

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp
import me.brandom.schoolmanager.utils.Constants

@HiltAndroidApp
class SchoolManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val reminderChannel = NotificationChannelCompat.Builder(
            Constants.HOMEWORK_REMINDER_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName("Homework reminders")
            .build()

        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(reminderChannel)
    }
}