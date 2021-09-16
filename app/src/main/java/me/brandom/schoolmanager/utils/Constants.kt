package me.brandom.schoolmanager.utils

import java.time.format.DateTimeFormatter

object Constants {
    const val HOMEWORK_REMINDER_CHANNEL_ID = "com.brandom.schoolmanager.HOMEWORK_REMINDER_CHANNEL"
    val INTERNAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
}