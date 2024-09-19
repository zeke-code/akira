package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class HomeViewModel(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    private val _allTransactions: LiveData<List<TransactionModel>> = repository.allTransactions
    val allTransactions: LiveData<List<TransactionModel>> get() = _allTransactions

    private val _currencySymbol = MutableLiveData<String>()
    val currencySymbol: LiveData<String> get() = _currencySymbol

    // Filter transactions for the current month
    private val currentMonthTransactions: LiveData<List<TransactionModel>> = allTransactions.map { transactions ->
        transactions.filter { transaction ->
            when (transaction) {
                is TransactionModel.Expense -> isInCurrentMonth(transaction.expense.date)
                is TransactionModel.Earning -> isInCurrentMonth(transaction.revenue.date)
            }
        }
    }

    val remainingMonthlyBudget: LiveData<Float> = currentMonthTransactions.map { transactions ->
        val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.revenue.amount }
        val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expense.amount }
        val userBudget = _monthlyBudget.value ?: 0F

        (userBudget - totalExpenses + totalEarnings).toFloat()
    }

     val usedBudgetPercentage: LiveData<Float> = remainingMonthlyBudget.map { remainingBudget ->
        val totalBudget = _monthlyBudget.value ?: 0F
        if (totalBudget > 0F) {
            ((totalBudget - remainingBudget) / totalBudget) * 100
        } else {
            0F
        }
    }

    // Extract expenses and revenues in one transformation
    private val expensesAndRevenues: LiveData<Pair<List<TransactionModel.Expense>, List<TransactionModel.Earning>>> = allTransactions.map { transactions ->
        val expenses = transactions.filterIsInstance<TransactionModel.Expense>()
        val revenues = transactions.filterIsInstance<TransactionModel.Earning>()
        expenses to revenues
    }

    // Utility function to check if a transaction is in the current month
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
        if (key == "MonthlyBudget") {
            _monthlyBudget.value = sharedPreferences.getFloat(key, 0F)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

