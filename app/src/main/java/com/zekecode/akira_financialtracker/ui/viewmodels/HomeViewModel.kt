package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class HomeViewModel(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    private val _chartModelProducer = CartesianChartModelProducer()
    val chartModelProducer = _chartModelProducer

    val allTransactions: LiveData<List<TransactionModel>> = repository.allTransactions
    val expenses: LiveData<List<TransactionModel.Expense>> = allTransactions.map { transactions ->
        transactions.filterIsInstance<TransactionModel.Expense>()
    }
    val revenues: LiveData<List<TransactionModel.Earning>> = allTransactions.map { transactions ->
        transactions.filterIsInstance<TransactionModel.Earning>()
    }

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
    val remainingMonthlyBudget: LiveData<Float> = currentMonthTransactions.map { transactions ->
        val totalEarnings = transactions.filterIsInstance<TransactionModel.Earning>().sumOf { it.revenue.amount }
        val totalExpenses = transactions.filterIsInstance<TransactionModel.Expense>().sumOf { it.expense.amount }

        val userBudget = _monthlyBudget.value ?: 0F

        (userBudget - totalExpenses + totalEarnings).toFloat()
    }

    init {
        fetchUserData()
        allTransactions.observeForever { transactions ->
            updateChartData(transactions)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _monthlyBudget.value = sharedPreferences.getFloat("MonthlyBudget", 0F)
        }
    }

    private fun updateChartData(transactions: List<TransactionModel>?) {
        viewModelScope.launch {
            val expenseByCategory: Map<String, Double> = transactions
                ?.filterIsInstance<TransactionModel.Expense>()
                ?.groupBy { it.expense.category }
                ?.mapValues { (_, expenses) ->
                    expenses.sumOf { it.expense.amount }
                } ?: emptyMap()

            val categories = expenseByCategory.keys.toList()
            val categoriesListKey = ExtraStore.Key<List<String>>()
            val amounts = expenseByCategory.values.toList()

            Log.d("HomeViewModel", "Expense Categories: $categories")
            Log.d("HomeViewModel", "Expense Categories: $categoriesListKey")
            Log.d("HomeViewModel", "Expense Amounts: $amounts")

            if (categories.isNotEmpty() && amounts.isNotEmpty()) {
                _chartModelProducer.runTransaction {
                    extras { it[categoriesListKey] = expenseByCategory.keys.toList() }
                    columnSeries {
                        series(amounts)
                    }
                }
            } else {
                Log.w("HomeViewModel", "Empty data: not updating chart.")
            }
            CartesianValueFormatter { x, chartValues, _ ->
                chartValues.model.extraStore[categoriesListKey][x.toInt()]
            }
        }
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
