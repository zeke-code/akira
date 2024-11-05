package com.zekecode.akira_financialtracker

import android.app.Application
import com.zekecode.akira_financialtracker.services.managers.AppNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AkiraApplication : Application() {

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    override fun onCreate() {
        super.onCreate()
        appNotificationManager.createNotificationChannel(
            "reminder_channel",
            "Reminder Notifications",
            description = "Channel for reminder notifications"
            )
    }
}
