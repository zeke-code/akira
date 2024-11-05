package com.zekecode.akira_financialtracker.services.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.zekecode.akira_financialtracker.services.notifiers.DailyReminderNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager @Inject constructor(
    private val dailyReminderNotifier: DailyReminderNotifier,
    @ApplicationContext private val context: Context
) {

    fun showDailyLoggingReminder() {
        dailyReminderNotifier.showNotification()
    }

    /**
     * Creates a notification channel with customizable properties.
     *
     * @param channelId Unique ID for the notification channel
     * @param channelName Visible name of the notification channel in system settings
     * @param importance Importance level for notifications in this channel
     * @param description Description of the notification channel
     */
    fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        description: String? = null
    ) {
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description?.let { this.description = it }
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)
    }
}
