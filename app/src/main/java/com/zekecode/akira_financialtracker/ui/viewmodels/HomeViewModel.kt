package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currencySymbol: LiveData<String> = userRepository.currencySymbolLiveData
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _monthlyBudget: LiveData<Double?> = financialRepository.getMonthlyBudget()

    private val _currentMonthTransactions: LiveData<List<TransactionModel>> = financialRepository.getCurrentMonthTransactions()

    private val _remainingMonthlyBudget: LiveData<Double> = _monthlyBudget.switchMap { budget ->
        _currentMonthTransactions.map { transactions ->
            calculateRemainingBudget(budget, transactions)
        }
    }
    val remainingMonthlyBudget: LiveData<Double> get() = _remainingMonthlyBudget

    private val _usedBudgetPercentage: LiveData<Float> = _monthlyBudget.switchMap { budget ->
        _remainingMonthlyBudget.map { remainingBudget ->
            calculateUsedBudgetPercentage(budget, remainingBudget)
        }
    }
    val usedBudgetPercentage: LiveData<Float> get() = _usedBudgetPercentage

    private val _transactionFilter = MutableLiveData<Filter>(Filter.MONTHLY)

    val filteredTransactions: LiveData<List<TransactionModel>> = _transactionFilter.switchMap { filter ->
        _currentMonthTransactions.map { transactions ->
            filterTransactions(filter, transactions)
        }
    }

    fun setTransactionFilter(filter: Filter) {
        _transactionFilter.value = filter
    }

    fun updateTransaction(transaction: TransactionModel, description: String, amount: Double, date: Long, category: CategoryModel) {
        viewModelScope.launch {
            val updatedTransaction = when (transaction) {
                is TransactionModel.Expense -> transaction.copy(
                    expenseWithCategory = transaction.expenseWithCategory.copy(
                        expense = transaction.expenseWithCategory.expense.copy(
                            description = description,
                            amount = amount,
                            date = date,
                            categoryId = category.id
                        ),
                        category = category
                    )
                )
                is TransactionModel.Earning -> transaction.copy(
                    earningWithCategory = transaction.earningWithCategory.copy(
                        earning = transaction.earningWithCategory.earning.copy(
                            description = description,
                            amount = amount,
                            date = date,
                            categoryId = category.id
                        ),
                        category = category
                    )
                )
            }

            when (updatedTransaction) {
                is TransactionModel.Expense -> financialRepository.updateExpense(updatedTransaction.expenseWithCategory.expense)
                is TransactionModel.Earning -> financialRepository.updateEarning(updatedTransaction.earningWithCategory.earning)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionModel) {
        viewModelScope.launch {
            when (transaction) {
                is TransactionModel.Expense -> financialRepository.deleteExpense(transaction.expenseWithCategory.expense)
                is TransactionModel.Earning -> financialRepository.deleteEarning(transaction.earningWithCategory.earning)
            }
        }
    }

    fun addTransactionBack(transaction: TransactionModel) {
        viewModelScope.launch {
            when (transaction) {
                is TransactionModel.Expense -> financialRepository.insertExpense(transaction.expenseWithCategory.expense)
                is TransactionModel.Earning -> financialRepository.insertEarning(transaction.earningWithCategory.earning)
            }
        }
    }

    private fun calculateRemainingBudget(
        budget: Double?,
        transactions: List<TransactionModel>
    ): Double {
        val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>()
            .sumOf { it.earningWithCategory.earning.amount }
        val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>()
            .sumOf { it.expenseWithCategory.expense.amount }
        return (budget ?: 0.0) - totalExpenses + totalEarnings
    }

    private fun calculateUsedBudgetPercentage(
        budget: Double?,
        remainingBudget: Double
    ): Float {
        val totalBudget = budget ?: 0.0
        return if (totalBudget > 0) {
            (((totalBudget - remainingBudget) / totalBudget) * 100).toFloat()
        } else {
            0f
        }
    }

    private fun TransactionModel.getDate(): Long {
        return when (this) {
            is TransactionModel.Expense -> this.expenseWithCategory.expense.date
            is TransactionModel.Earning -> this.earningWithCategory.earning.date
        }
    }

    // Filter logic in HomeViewModel
    private fun filterTransactions(
        filter: Filter,
        transactions: List<TransactionModel>
    ): List<TransactionModel> {
        val calendar = Calendar.getInstance()

        return when (filter) {
            Filter.DAILY -> {
                // Set to start of day
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis

                // Set to end of day
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endOfDay = calendar.timeInMillis

                transactions.filter { it.getDate() in startOfDay..endOfDay }
            }
            Filter.WEEKLY -> {
                // Set to start of week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfWeek = calendar.timeInMillis

                // Set to end of week
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endOfWeek = calendar.timeInMillis

                transactions.filter { it.getDate() in startOfWeek..endOfWeek }
            }
            Filter.MONTHLY -> {
                // Set to start of month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfMonth = calendar.timeInMillis

                // Set to end of month
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endOfMonth = calendar.timeInMillis

                transactions.filter { it.getDate() in startOfMonth..endOfMonth }
            }
        }
    }

    enum class Filter {
        DAILY, WEEKLY, MONTHLY
    }
}