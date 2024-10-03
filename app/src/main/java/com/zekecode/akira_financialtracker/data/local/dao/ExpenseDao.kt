package com.zekecode.akira_financialtracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): LiveData<List<ExpenseModel>>

    @Transaction
    @Query("SELECT * FROM expenses")
    fun getExpensesWithCategories(): LiveData<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseModel)

    @Delete
    suspend fun deleteExpense(expense: ExpenseModel)
}
