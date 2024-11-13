package com.zekecode.akira_financialtracker.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.zekecode.akira_financialtracker.data.local.dao.BudgetDao
import com.zekecode.akira_financialtracker.data.local.dao.CategoryDao
import com.zekecode.akira_financialtracker.data.local.dao.EarningDao
import com.zekecode.akira_financialtracker.data.local.dao.ExpenseDao
import com.zekecode.akira_financialtracker.data.local.entities.*
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val earningDao: EarningDao,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
) {
    private val _allExpensesWithCategory = expenseDao.getAllExpensesWithCategories()
    private val _allEarningsWithCategory = earningDao.getAllEarningsWithCategories()

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

    // Methods to expose RoomDB data
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

    fun getMonthlyExpenses(yearMonth: String): LiveData<List<ExpenseWithCategory>> {
        return expenseDao.getMonthlyExpensesWithCategories(yearMonth)
    }

    fun getMonthlyEarnings(yearMonth: String): LiveData<List<EarningWithCategory>> {
        return earningDao.getMonthlyEarningsWithCategories(yearMonth)
    }

    fun getCurrentMonthTransactions(): LiveData<List<TransactionModel>> {
        val earningsLiveData = getMonthlyEarnings(getCurrentYearMonth())
        val expensesLiveData = getMonthlyExpenses(getCurrentYearMonth())

        val currentMonthTransactions = MediatorLiveData<List<TransactionModel>>()
        currentMonthTransactions.addSource(earningsLiveData) { earnings ->
            val currentExpenses = expensesLiveData.value ?: emptyList()
            currentMonthTransactions.value = mergeTransactions(currentExpenses, earnings)
        }

        currentMonthTransactions.addSource(expensesLiveData) { expenses ->
            val currentEarnings = earningsLiveData.value ?: emptyList()
            currentMonthTransactions.value = mergeTransactions(expenses, currentEarnings)
        }
        return currentMonthTransactions
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
