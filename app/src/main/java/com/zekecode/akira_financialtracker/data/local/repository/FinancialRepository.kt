package com.zekecode.akira_financialtracker.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel

class FinancialRepository(
    private val expenseDao: ExpenseDao,
    private val earningDao: EarningDao,
    private val categoryDao: CategoryDao
) {
    val allExpenses: LiveData<List<ExpenseModel>> = expenseDao.getAllExpenses()
    val allEarnings: LiveData<List<EarningModel>> = earningDao.getAllEarnings()
    val allCategories: LiveData<List<CategoryModel>> = categoryDao.getAllCategories()

    val allTransactions: LiveData<List<TransactionModel>> = MediatorLiveData<List<TransactionModel>>().apply {
        addSource(allExpenses) { expenses ->
            val earnings = allEarnings.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
        addSource(allEarnings) { earnings ->
            val expenses = allExpenses.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
    }

    private fun mergeTransactions(
        expenses: List<ExpenseModel>,
        earnings: List<EarningModel>
    ): List<TransactionModel> {
        val transactions = mutableListOf<TransactionModel>()
        transactions.addAll(expenses.map { TransactionModel.Expense(it) })
        transactions.addAll(earnings.map { TransactionModel.Earning(it) })
        return transactions.sortedByDescending {
            when (it) {
                is TransactionModel.Expense -> it.expense.date
                is TransactionModel.Earning -> it.revenue.date
            }
        }
    }

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

    suspend fun insertCategory(category: CategoryModel) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryModel) {
        categoryDao.deleteCategory(category)
    }

}