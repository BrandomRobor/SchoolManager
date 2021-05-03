package me.brandom.schoolmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HomeworkBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val HOMEWORK_REMINDERS_CHANNEL_ID =
            "me.brandom.schoolmanager.HOMEWORK_REMINDERS_CHANNEL"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

    }
}