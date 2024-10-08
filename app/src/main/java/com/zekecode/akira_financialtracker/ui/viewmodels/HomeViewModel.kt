package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
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

    private val _currentMonthTransactions = MutableLiveData<List<TransactionModel>>()
    val currentMonthTransactions: LiveData<List<TransactionModel>> get() = _currentMonthTransactions

    val remainingMonthlyBudget: LiveData<Double> = monthlyBudget.switchMap { budget ->
        currentMonthTransactions.map { transactions ->
            val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.earningWithCategory.earning.amount }
            val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expenseWithCategory.expense.amount }
            (budget ?: 0.0) - totalExpenses + totalEarnings
        }
    }

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
        fetchCurrentMonthBudgetAndTransactions()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun fetchCurrencySymbol() {
        viewModelScope.launch(Dispatchers.IO) {
            _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
        }
    }

    private fun fetchCurrentMonthBudgetAndTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val currentYearMonth = getCurrentYearMonth()
            val monthlyEarnings = repository.getMonthlyEarnings(currentYearMonth)
            val monthlyExpenses = repository.getMonthlyExpenses(currentYearMonth)

            val earningTransactions = monthlyEarnings.map { TransactionModel.Earning(it) }
            val expenseTransactions = monthlyExpenses.map { TransactionModel.Expense(it) }

            _currentMonthTransactions.postValue(earningTransactions + expenseTransactions)
            _isLoading.postValue(false)
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
