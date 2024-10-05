package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatsViewModel(
    private val repository: FinancialRepository
): ViewModel() {
    private val _allExpenses: LiveData<List<ExpenseWithCategory>> = repository.allExpensesWithCategory
    val allExpenses: LiveData<List<ExpenseWithCategory>> get() = _allExpenses

    private val _allEarnings: LiveData<List<EarningWithCategory>> = repository.allEarningsWithCategory
    val allEarnings: LiveData<List<EarningWithCategory>> get() = _allEarnings

    private val _chartModelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    val chartModelProducer: CartesianChartModelProducer get() = _chartModelProducer

    private val _revenueChartModelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    val revenueChartModelProducer: CartesianChartModelProducer get() = _revenueChartModelProducer

    val _bottomAxisValueFormatter = MutableLiveData<CartesianValueFormatter>()

    init {
        allExpenses.observeForever { expenses ->
            viewModelScope.launch(Dispatchers.IO) {
                updateExpenseChart(expenses)
            }
        }

        allEarnings.observeForever { earnings ->
            viewModelScope.launch(Dispatchers.IO) {
                updateRevenueChart(earnings)
            }
        }
    }

    private suspend fun updateExpenseChart(expenses: List<ExpenseWithCategory>) {
        // Check if the passed list is empty
        if (expenses.isEmpty()) {
            Log.w("StatsViewModel", "No expenses available to update the chart.")
            return  // Exit early if the expenses list is empty
        }

        // Group the expenses by their category names
        val groupedExpenses = expenses.groupBy { it.category.name }
        val categoryNames = groupedExpenses.keys.toList()

        // Calculate the sum for each category group
        val categorySums = groupedExpenses.values.map { group ->
            group.sumOf { it.expense.amount }.toFloat()
        }

        Log.d("StatsViewModel", "CategorySums: $categorySums")

        // Define a key for the label list to be used in the chart’s extra store
        val labelListKey = ExtraStore.Key<List<String>>()

        // Update the chart model producer with the new data
        _chartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }

        val categoryFormatter = CartesianValueFormatter { context, x, _ ->
            context.model.extraStore[labelListKey].getOrNull(x.toInt()) ?: x.toString()
        }
    }

    private suspend fun updateRevenueChart(earnings: List<EarningWithCategory>) {
        // Check if the passed list is empty
        if (earnings.isEmpty()) {
            Log.w("StatsViewModel", "No earnings available to update the chart.")
            return  // Exit early if the earnings list is empty
        }

        // Group the earnings by their category names
        val groupedEarnings = earnings.groupBy { it.category.name }
        val categoryNames = groupedEarnings.keys.toList()

        // Calculate the sum for each category group
        val categorySums = groupedEarnings.values.map { group ->
            group.sumOf { it.earning.amount }.toFloat()
        }

        Log.d("StatsViewModel", "CategorySums: $categorySums")

        // Define a key for the label list to be used in the chart’s extra store
        val labelListKey = ExtraStore.Key<List<String>>()

        // Update the revenue chart model producer with the new data
        _revenueChartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }

        // Use a value formatter to display category names on the chart
        val categoryFormatter = CartesianValueFormatter { context, x, _ ->
            context.model.extraStore[labelListKey].getOrNull(x.toInt()) ?: x.toString()
        }

        _bottomAxisValueFormatter.value = categoryFormatter
    }

}