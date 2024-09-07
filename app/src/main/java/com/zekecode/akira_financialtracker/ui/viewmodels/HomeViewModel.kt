package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import java.util.Calendar
import java.util.Date

class HomeViewModel(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    val allTransactions: LiveData<List<TransactionModel>> = repository.allTransactions

    private val currentMonthTransactions: LiveData<List<TransactionModel>> = allTransactions.map { transactions ->
        transactions.filter { transaction ->
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val transactionCalendar = Calendar.getInstance()
            when (transaction) {
                is TransactionModel.Expense -> {
                    transactionCalendar.time = Date(transaction.expense.date) // Convert Long to Date
                }
                is TransactionModel.Earning -> {
                    transactionCalendar.time = Date(transaction.revenue.date) // Convert Long to Date
                }
            }

            val transactionYear = transactionCalendar.get(Calendar.YEAR)
            val transactionMonth = transactionCalendar.get(Calendar.MONTH)

            transactionYear == currentYear && transactionMonth == currentMonth
        }
    }

    // Calculate the real monthly budget based on filtered transactions using the extension function `map`
    val realMonthlyBudget: LiveData<Float> = currentMonthTransactions.map { transactions ->
        val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.revenue.amount }
        val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expense.amount }
        (totalEarnings - totalExpenses).toFloat()
    }

    init {
        fetchUserData()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun fetchUserData() {
        _monthlyBudget.value = sharedPreferences.getFloat("MonthlyBudget", 0F)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "MonthlyBudget" -> {
                _monthlyBudget.value = sharedPreferences.getFloat(key, 0F)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
