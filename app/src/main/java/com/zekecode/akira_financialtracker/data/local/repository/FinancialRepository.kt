package com.zekecode.akira_financialtracker.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.zekecode.akira_financialtracker.data.local.dao.BudgetDao
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.entities.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val earningDao: EarningDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {
    // Private LiveData properties
    private val _allExpensesWithCategory = expenseDao.getExpensesWithCategories()
    private val _allEarningsWithCategory = earningDao.getEarningsWithCategories()

    private val _allTransactions = MediatorLiveData<List<TransactionModel>>().apply {
        addSource(_allExpensesWithCategory) { expenses ->
            val earnings = _allEarningsWithCategory.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
        addSource(_allEarningsWithCategory) { earnings ->
            val expenses = _allExpensesWithCategory.value ?: emptyList()
            value = mergeTransactions(expenses, earnings)
        }
    }

    // Functions to expose data
    fun getAllTransactions(): LiveData<List<TransactionModel>> = _allTransactions

    fun getAllCategories(): LiveData<List<CategoryModel>> = categoryDao.getAllCategories()

    fun getAllEarnings(): LiveData<List<EarningModel>> = earningDao.getAllEarnings()

    fun getAllExpenses(): LiveData<List<ExpenseModel>> = expenseDao.getAllExpenses()

    fun getAllExpensesWithCategory(): LiveData<List<ExpenseWithCategory>> = _allExpensesWithCategory

    fun getAllEarningsWithCategory(): LiveData<List<EarningWithCategory>> = _allEarningsWithCategory

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

    suspend fun getMonthlyExpenses(yearMonth: String): List<ExpenseWithCategory> {
        return expenseDao.getMonthlyExpensesWithCategories(yearMonth)
    }

    suspend fun getMonthlyEarnings(yearMonth: String): List<EarningWithCategory> {
        return earningDao.getMonthlyEarningsWithCategories(yearMonth)
    }

    suspend fun insertCategory(category: CategoryModel) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryModel) {
        categoryDao.deleteCategory(category)
    }

    // Budget-related methods
    suspend fun insertBudget(budget: BudgetModel) {
        budgetDao.insertBudget(budget)
    }

    fun getMonthlyBudget(yearMonth: String): LiveData<Double?> {
        return budgetDao.getMonthlyBudget(yearMonth)
    }

    // Private function to merge expenses and earnings
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
}
