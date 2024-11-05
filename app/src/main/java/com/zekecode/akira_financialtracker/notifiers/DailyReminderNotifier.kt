package com.zekecode.akira_financialtracker.notifiers

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.zekecode.akira_financialtracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DailyReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : Notifier {

    override fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notification = NotificationCompat.Builder(context, "daily_reminder_channel")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Daily Reminder")
            .setContentText("Have you registered your transactions today?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }
}
