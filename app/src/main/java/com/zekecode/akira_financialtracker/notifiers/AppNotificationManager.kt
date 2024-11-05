package com.zekecode.akira_financialtracker.notifiers

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppNotificationManager @Inject constructor(
    private val dailyReminderNotifier: DailyReminderNotifier,
) {

    fun showDailyLoggingReminder() {
        dailyReminderNotifier.showNotification()
    }
}
