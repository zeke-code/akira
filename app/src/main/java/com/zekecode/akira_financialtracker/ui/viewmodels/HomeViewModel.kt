package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    private val _allTransactions: LiveData<List<TransactionModel>> = repository.getAllTransactions()
    val allTransactions: LiveData<List<TransactionModel>> get() = _allTransactions

    private val _currencySymbol = MutableLiveData<String>()
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Filter transactions for the current month
    private val currentMonthTransactions: LiveData<List<TransactionModel>> = allTransactions.map { transactions ->
        transactions.filter { transaction ->
            when (transaction) {
                is TransactionModel.Expense -> isInCurrentMonth(transaction.expenseWithCategory.expense.date)
                is TransactionModel.Earning -> isInCurrentMonth(transaction.earningWithCategory.earning.date)
            }
        }
    }

    val remainingMonthlyBudget: LiveData<Float> = currentMonthTransactions.map { transactions ->
        val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.earningWithCategory.earning.amount }
        val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expenseWithCategory.expense.amount }
        val userBudget = _monthlyBudget.value ?: 0F

        (userBudget - totalExpenses + totalEarnings).toFloat()
    }

    val usedBudgetPercentage: LiveData<Float> = remainingMonthlyBudget.map { remainingBudget ->
        _isLoading.value = true
        val totalBudget = _monthlyBudget.value ?: 0F
        val percentage = if (totalBudget > 0F) {
            ((totalBudget - remainingBudget) / totalBudget) * 100
        } else {
            0F
        }
        _isLoading.value = false
        percentage
    }

    private fun isInCurrentMonth(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val transactionCalendar = Calendar.getInstance().apply {
            time = Date(timestamp)
        }

        val transactionYear = transactionCalendar.get(Calendar.YEAR)
        val transactionMonth = transactionCalendar.get(Calendar.MONTH)

        return transactionYear == currentYear && transactionMonth == currentMonth
    }

    init {
        fetchUserData()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun fetchUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            _monthlyBudget.postValue(sharedPreferences.getFloat("MonthlyBudget", 0F))
            _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (key) {
                "MonthlyBudget" -> _monthlyBudget.postValue(sharedPreferences.getFloat(key, 0F))
                "Currency" -> _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
