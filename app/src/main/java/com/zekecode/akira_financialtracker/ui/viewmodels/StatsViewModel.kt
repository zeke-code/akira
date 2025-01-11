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

    private val _sumsChartModelProducer = CartesianChartModelProducer()
    val sumsChartModelProducer: CartesianChartModelProducer
        get() = _sumsChartModelProducer

    // Axis label keys for each chart
    val categoriesLabelList = ExtraStore.Key<List<String>>()
    val dateLabelList = ExtraStore.Key<List<String>>()

    // Category chart data
    private var categoryNamesExpense: List<String> = emptyList()
    private var categorySumsExpense: List<Double> = emptyList()
    private var categoryNamesRevenue: List<String> = emptyList()
    private var categorySumsRevenue: List<Double> = emptyList()

    private var isShowingRevenueForSums = false
    private var dailyExpenseSums: List<Double> = emptyList()
    private var expenseDays: List<String> = emptyList()
    private var dailyRevenueSums: List<Double> = emptyList()
    private var revenueDays: List<String> = emptyList()

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
     * Process monthly expenses by day
     */
    fun processExpenseSumsByDay(items: List<ExpenseWithCategory>?) {
        if (items.isNullOrEmpty()) return

        val dailyGrouped = items.groupBy {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.expense.date }
            calendar.get(Calendar.DAY_OF_MONTH)
        }
        expenseDays = dailyGrouped.keys.sorted().map { it.toString() }
        dailyExpenseSums = expenseDays.map { day ->
            dailyGrouped[day.toInt()]?.sumOf { it.expense.amount } ?: 0.0
        }

        // Only update if we're showing expenses
        if (!isShowingRevenueForSums) {
            updateSumsChart(showRevenues = false)
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
     * Process monthly earnings by day
     */
    fun processEarningsSumsByDay(items: List<EarningWithCategory>?) {
        if (items.isNullOrEmpty()) return

        val dailyGrouped = items.groupBy {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.earning.date }
            calendar.get(Calendar.DAY_OF_MONTH)
        }
        revenueDays = dailyGrouped.keys.sorted().map { it.toString() }
        dailyRevenueSums = revenueDays.map { day ->
            dailyGrouped[day.toInt()]?.sumOf { it.earning.amount } ?: 0.0
        }

        // Only update if we're showing revenues
        if (isShowingRevenueForSums) {
            updateSumsChart(showRevenues = true)
        }
    }

    /**
     * Update the sums chart to show either expenses or revenues
     */
    fun updateSumsChart(showRevenues: Boolean) {
        isShowingRevenueForSums = showRevenues

        val chosenDays = if (showRevenues) revenueDays else expenseDays
        val chosenSums = if (showRevenues) dailyRevenueSums else dailyExpenseSums

        viewModelScope.launch {
            _sumsChartModelProducer.runTransaction {
                lineSeries { series(chosenSums) }
                extras { extraStore ->
                    extraStore[dateLabelList] = chosenDays
                }
            }
        }
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