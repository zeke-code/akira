package com.zekecode.akira_financialtracker.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zekecode.akira_financialtracker.notifiers.AppNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appNotificationManager: AppNotificationManager
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        appNotificationManager.showDailyLoggingReminder()
        return Result.success()
    }
}
