package com.zekecode.akira_financialtracker.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.entities.*

class FinancialRepository(
    private val expenseDao: ExpenseDao,
    private val earningDao: EarningDao,
    private val categoryDao: CategoryDao
) {
    private val _allExpensesWithCategory: LiveData<List<ExpenseWithCategory>> = expenseDao.getExpensesWithCategories()
    private val _allEarningsWithCategory: LiveData<List<EarningWithCategory>> = earningDao.getEarningsWithCategories()
    private val _allEarnings: LiveData<List<EarningModel>> = earningDao.getAllEarnings()
    private val _allExpenses: LiveData<List<ExpenseModel>> = expenseDao.getAllExpenses()
    private val _allCategories: LiveData<List<CategoryModel>> = categoryDao.getAllCategories()

    private val _allTransactions: MediatorLiveData<List<TransactionModel>> = MediatorLiveData<List<TransactionModel>>().apply {
        addSource(_allExpensesWithCategory) { expenses ->
            val earnings = _allEarningsWithCategory.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
        addSource(_allEarningsWithCategory) { earnings ->
            val expenses = _allExpensesWithCategory.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
    }
    val allTransactions: LiveData<List<TransactionModel>> get() = _allTransactions
    val allCategories: LiveData<List<CategoryModel>> get() = _allCategories
    val allEarnings: LiveData<List<EarningModel>> get() = _allEarnings
    val allExpenses: LiveData<List<ExpenseModel>> get() = _allExpenses
    val allExpensesWithCategory: LiveData<List<ExpenseWithCategory>> get() = _allExpensesWithCategory
    val allEarningsWithCategory: LiveData<List<EarningWithCategory>> get() = _allEarningsWithCategory

    private fun mergeTransactions(
        expenses: List<ExpenseWithCategory>,
        earnings: List<EarningWithCategory>
    ): List<TransactionModel> {
        val transactions = mutableListOf<TransactionModel>()
        transactions.addAll(expenses.map { TransactionModel.Expense(it) })
        transactions.addAll(earnings.map { TransactionModel.Earning(it) })
        return transactions.sortedByDescending {
            when (it) {
                is TransactionModel.Expense -> it.expenseWithCategory.expense.date
                is TransactionModel.Earning -> it.earningWithCategory.earning.date
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
