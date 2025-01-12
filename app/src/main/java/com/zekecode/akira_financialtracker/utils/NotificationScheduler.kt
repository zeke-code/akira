package com.zekecode.akira_financialtracker.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zekecode.akira_financialtracker.workers.DailyReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(private val context: Context) {

    companion object {
        private const val DAILY_REMINDER_WORK = "daily_reminder_work"
    }

    fun scheduleDailyReminder(hourOfDay: Int = 16) { // Default to 4 PM
        // Cancel any existing work
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_REMINDER_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWorkRequest
            )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)
    }
}