package com.zekecode.akira_financialtracker.di

import android.content.Context
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AkiraDatabase {
        val applicationScope = CoroutineScope(SupervisorJob())
        return AkiraDatabase.getDatabase(
            context = context,
            scope = applicationScope
        )
    }

    @Provides
    fun provideExpenseDao(database: AkiraDatabase) = database.expenseDao()

    @Provides
    fun provideEarningDao(database: AkiraDatabase) = database.earningDao()

    @Provides
    fun provideCategoryDao(database: AkiraDatabase) = database.categoryDao()

    @Provides
    fun provideBudgetDao(database: AkiraDatabase) = database.budgetDao()

    @Provides
    @Singleton
    fun provideFinancialRepository(
        database: AkiraDatabase,
    ): FinancialRepository {
        return FinancialRepository(
            database.expenseDao(),
            database.earningDao(),
            database.budgetDao(),
            database.categoryDao()
        )
    }
}
