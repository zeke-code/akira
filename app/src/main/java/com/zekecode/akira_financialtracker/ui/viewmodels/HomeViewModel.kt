package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _currencySymbol = MutableLiveData<String>()
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val monthlyBudget: LiveData<Double?> = repository.getMonthlyBudget(getCurrentYearMonth())

    // Use MediatorLiveData to combine earnings and expenses
    private val _currentMonthTransactions = MediatorLiveData<List<TransactionModel>>()
    val currentMonthTransactions: LiveData<List<TransactionModel>> get() = _currentMonthTransactions

    // The remaining budget after expenses and earnings are applied
    val remainingMonthlyBudget: LiveData<Double> = monthlyBudget.switchMap { budget ->
        currentMonthTransactions.map { transactions ->
            val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.earningWithCategory.earning.amount }
            val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expenseWithCategory.expense.amount }
            (budget ?: 0.0) - totalExpenses + totalEarnings
        }
    }

    // Calculate the used budget percentage
    val usedBudgetPercentage: LiveData<Float> = remainingMonthlyBudget.map { remainingBudget ->
        val totalBudget = monthlyBudget.value ?: 0.0
        if (totalBudget > 0) {
            (((totalBudget - remainingBudget) / totalBudget) * 100).toFloat()
        } else {
            0f
        }
    }

    init {
        fetchCurrencySymbol()
        viewModelScope.launch {
            setupTransactionMerging()
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private suspend fun setupTransactionMerging() {
        val earningsLiveData = repository.getMonthlyEarnings(getCurrentYearMonth())
        val expensesLiveData = repository.getMonthlyExpenses(getCurrentYearMonth())

        _currentMonthTransactions.addSource(earningsLiveData) { earnings ->
            val currentExpenses = expensesLiveData.value ?: emptyList()
            _currentMonthTransactions.value = mergeTransactions(currentExpenses, earnings)
        }

        _currentMonthTransactions.addSource(expensesLiveData) { expenses ->
            val currentEarnings = earningsLiveData.value ?: emptyList()
            _currentMonthTransactions.value = mergeTransactions(expenses, currentEarnings)
        }
    }


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

    private fun fetchCurrencySymbol() {
        viewModelScope.launch(Dispatchers.IO) {
            _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (key) {
                "Currency" -> {
                    _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
