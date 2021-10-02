package me.brandom.schoolmanager.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.brandom.schoolmanager.receivers.HomeworkReminderReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    // Temporary array of hardcoded values for testing
    // It will be configurable later
    // Values:                      3 days   , 1 day
    private val tempArray = arrayOf(259200000, 86400000L)
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)!!
    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT else PendingIntent.FLAG_CANCEL_CURRENT

    fun createReminderAlarm(id: Int, deadline: Long) {
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            deadline,
            createPendingIntent(id)
        )
    }

    fun cancelReminderAlarm(id: Int) {
        alarmManager.cancel(createPendingIntent(id))
    }

    private fun createPendingIntent(id: Int): PendingIntent {
        val intent = Intent(context, HomeworkReminderReceiver::class.java)
        intent.putExtra("id", id)

        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            pendingIntentFlags
        )
    }

    fun getInitialReminderTime(deadline: Long) = deadline - tempArray[0]

    fun getNextReminderTime(deadline: Long) =
        deadline - (tempArray.find { (deadline - System.currentTimeMillis()) > it }
            ?: 0)
}