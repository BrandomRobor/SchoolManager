package me.brandom.schoolmanager.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTimeHelper @Inject constructor() {
    // Temporary array of hardcoded values for testing
    // It will be configurable later
    // Values:                      3 days   , 1 day
    private val tempArray = arrayOf(259200000, 86400000L)

    fun getInitialNotificationTime(deadline: Long) = deadline - tempArray[0]
}