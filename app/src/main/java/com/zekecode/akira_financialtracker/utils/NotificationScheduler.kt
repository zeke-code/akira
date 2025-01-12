package com.zekecode.akira_financialtracker.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.zekecode.akira_financialtracker.workers.DailyReminderWorker
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(private val context: Context) {

    companion object {
        private const val DAILY_REMINDER_WORK = "daily_reminder_work"
    }

    fun scheduleDailyReminder(hourOfDay: Int = 18, minute: Int = 0) {
        try {
            // First check existing work
            checkWorkStatus()

            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)

            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = calendar.timeInMillis - now

            Log.d("NotificationScheduler", """
                Scheduling daily reminder:
                Current time: ${Date(now)}
                Target time: ${Date(calendar.timeInMillis)}
                Initial delay: ${initialDelay / (1000 * 60)} minutes
            """.trimIndent())

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag("daily_reminder_tag")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    DAILY_REMINDER_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,  // Changed from UPDATE to KEEP
                    dailyWorkRequest
                )

            // Check status immediately after scheduling
            checkWorkStatus()
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error scheduling reminder", e)
        }
    }

    fun checkWorkStatus() {
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfosForUniqueWork(DAILY_REMINDER_WORK).get()

        workInfo.forEach { info ->
            Log.d("NotificationScheduler", """
                Work Status Check:
                ID: ${info.id}
                State: ${info.state}
                Tags: ${info.tags}
                Run Attempt Count: ${info.runAttemptCount}
            """.trimIndent())
        }
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)
        Log.d("NotificationScheduler", "Daily reminder cancelled")
    }
}
