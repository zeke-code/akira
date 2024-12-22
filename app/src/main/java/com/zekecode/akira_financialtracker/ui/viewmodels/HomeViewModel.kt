package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currencySymbol: LiveData<String> = userRepository.currencySymbolLiveData
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _monthlyBudget: LiveData<Double?> = financialRepository.getMonthlyBudget(getCurrentYearMonth())

    private val _currentMonthTransactions: LiveData<List<TransactionModel>> = financialRepository.getCurrentMonthTransactions()
    val currentMonthTransactions: LiveData<List<TransactionModel>> get() = _currentMonthTransactions

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

    fun updateTransaction(transaction: TransactionModel, description: String, amount: Double, date: Long) {
        viewModelScope.launch {
            val updatedTransaction = when (transaction) {
                is TransactionModel.Expense -> transaction.copy(
                    expenseWithCategory = transaction.expenseWithCategory.copy(
                        expense = transaction.expenseWithCategory.expense.copy(
                            description = description,
                            amount = amount,
                            date = date
                        )
                    )
                )
                is TransactionModel.Earning -> transaction.copy(
                    earningWithCategory = transaction.earningWithCategory.copy(
                        earning = transaction.earningWithCategory.earning.copy(
                            description = description,
                            amount = amount,
                            date = date
                        )
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
}