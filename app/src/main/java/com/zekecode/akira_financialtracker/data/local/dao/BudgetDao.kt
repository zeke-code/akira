package com.zekecode.akira_financialtracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetModel)

    @Query("SELECT amount FROM budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getMonthlyBudget(yearMonth: String): LiveData<Double?>
}
