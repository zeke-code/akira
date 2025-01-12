package com.zekecode.akira_financialtracker

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.zekecode.akira_financialtracker.utils.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AkiraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(applicationContext).cancelAllWork() // Clear any existing work

        // Check work status after initialization
        Handler(Looper.getMainLooper()).postDelayed({
            notificationScheduler.checkWorkStatus()
        }, 5000)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}