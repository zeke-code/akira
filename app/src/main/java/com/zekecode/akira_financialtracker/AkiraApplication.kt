package com.zekecode.akira_financialtracker

import android.app.Application
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class AkiraApplication : Application()
