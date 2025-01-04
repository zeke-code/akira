package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    val monthlyExpenses: LiveData<List<ExpenseWithCategory>> =
        repository.getMonthlyExpenses(getCurrentYearMonth())
    val monthlyEarnings: LiveData<List<EarningWithCategory>> =
        repository.getMonthlyEarnings(getCurrentYearMonth())

    private val _categoryChartModelProducer = CartesianChartModelProducer()
    val categoryChartModelProducer: CartesianChartModelProducer
        get() = _categoryChartModelProducer

    private val _expenseSumsChartModelProducer = CartesianChartModelProducer()
    val expenseSumsChartModelProducer: CartesianChartModelProducer
        get() = _expenseSumsChartModelProducer

    // Axis label keys for each chart
    val categoriesLabelList = ExtraStore.Key<List<String>>()
    val dateLabelList = ExtraStore.Key<List<String>>()

    // Category chart data
    private var categoryNamesExpense: List<String> = emptyList()
    private var categorySumsExpense: List<Double> = emptyList()

    private var categoryNamesRevenue: List<String> = emptyList()
    private var categorySumsRevenue: List<Double> = emptyList()

    // For enabling/disabling UI if data is missing
    private val _isDataAvailable = MutableLiveData(false)
    val isDataAvailable: LiveData<Boolean> get() = _isDataAvailable

    /**
     * Process monthly expenses grouped by category (fills categoryNamesExpense, categorySumsExpense).
     */
    fun processExpenses(items: List<ExpenseWithCategory>?) {
        if (!items.isNullOrEmpty()) {
            val groupedData = items.groupBy { it.category.name }
            categoryNamesExpense = groupedData.keys.toList()
            categorySumsExpense = groupedData.values.map { grp -> grp.sumOf { it.expense.amount } }
        } else {
            categoryNamesExpense = emptyList()
            categorySumsExpense = emptyList()
        }

        // By default, show expenses in the category chart
        updateCategoryChart(showRevenues = false)
        checkIfDataExists()
    }

    /**
     * Process monthly expenses by day (fills daily sums for the second chart).
     */
    fun processExpenseSumsByDay(items: List<ExpenseWithCategory>?) {
        if (items.isNullOrEmpty()) return

        val dailyGrouped = items.groupBy {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.expense.date }
            calendar.get(Calendar.DAY_OF_MONTH)
        }
        val sortedDays = dailyGrouped.keys.sorted()
        val dailySums = sortedDays.map { day -> dailyGrouped[day]?.sumOf { it.expense.amount } ?: 0.0 }

        viewModelScope.launch {
            _expenseSumsChartModelProducer.runTransaction {
                lineSeries { series(dailySums) }
                extras { extraStore ->
                    extraStore[dateLabelList] = sortedDays.map { it.toString() }
                }
            }
        }
    }

    /**
     * Process monthly earnings grouped by category (fills categoryNamesRevenue, categorySumsRevenue).
     */
    fun processEarnings(items: List<EarningWithCategory>?) {
        if (!items.isNullOrEmpty()) {
            val groupedData = items.groupBy { it.category.name }
            categoryNamesRevenue = groupedData.keys.toList()
            categorySumsRevenue = groupedData.values.map { grp -> grp.sumOf { it.earning.amount } }
        } else {
            categoryNamesRevenue = emptyList()
            categorySumsRevenue = emptyList()
        }
        checkIfDataExists()
    }

    /**
     * Update the category chart to show either expenses or revenues.
     * This version matches the toggleCharts() usage with a boolean "showRevenues" flag.
     */
    fun updateCategoryChart(showRevenues: Boolean) {
        val chosenNames = if (showRevenues) categoryNamesRevenue else categoryNamesExpense
        val chosenSums = if (showRevenues) categorySumsRevenue else categorySumsExpense

        viewModelScope.launch {
            _categoryChartModelProducer.runTransaction {
                columnSeries { series(chosenSums) }
                extras { extraStore ->
                    extraStore[categoriesLabelList] = chosenNames
                }
            }
        }
    }

    /**
     * Check if both expense and revenue category data exist, to manage UI visibility.
     */
    private fun checkIfDataExists() {
        _isDataAvailable.value =
            categoryNamesExpense.isNotEmpty() && categoryNamesRevenue.isNotEmpty()
    }
}