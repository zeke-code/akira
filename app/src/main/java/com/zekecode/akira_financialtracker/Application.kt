package com.zekecode.akira_financialtracker

import android.app.Application
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class Application : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy {
        AkiraDatabase.getDatabase(
            context = this,
            scope = applicationScope
        )
    }

    val repository by lazy {
        FinancialRepository(
            database.expenseDao(),
            database.earningDao(),
            database.categoryDao(),
            database.budgetDao())
    }

}
