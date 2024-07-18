package com.zekecode.akira_financialtracker.data.local.repository

import androidx.lifecycle.LiveData
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel

class FinancialRepository(
    private val expenseDao: ExpenseDao,
    private val earningDao: EarningDao
) {
    val allExpenses: LiveData<List<ExpenseModel>> = expenseDao.getAllExpenses()
    val allEarnings: LiveData<List<EarningModel>> = earningDao.getAllEarnings()

    suspend fun insertExpense(expense: ExpenseModel) {
        expenseDao.insertExpense(expense)
    }

    suspend fun insertEarning(earning: EarningModel) {
        earningDao.insertEarning(earning)
    }

    suspend fun deleteExpense(expense: ExpenseModel) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteEarning(earning: EarningModel) {
        earningDao.deleteEarning(earning)
    }
}