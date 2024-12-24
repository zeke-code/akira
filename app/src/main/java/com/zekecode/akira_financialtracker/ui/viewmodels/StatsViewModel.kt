package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    val monthlyExpenses: LiveData<List<ExpenseWithCategory>> =
        repository.getMonthlyExpenses(getCurrentYearMonth())
    val monthlyEarnings: LiveData<List<EarningWithCategory>> =
        repository.getMonthlyEarnings(getCurrentYearMonth())

    private val _expenseData = MutableLiveData<Pair<List<String>, List<Double>>>()
    val expenseData: LiveData<Pair<List<String>, List<Double>>> get() = _expenseData

    private val _earningData = MutableLiveData<Pair<List<String>, List<Double>>>()
    val earningData: LiveData<Pair<List<String>, List<Double>>> get() = _earningData

    private val _expenseChartModelProducer = CartesianChartModelProducer()
    val expenseChartModelProducer: CartesianChartModelProducer
        get() = _expenseChartModelProducer

    private val _earningChartModelProducer = CartesianChartModelProducer()
    val earningChartModelProducer: CartesianChartModelProducer
        get() = _earningChartModelProducer

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> get() = _isDataAvailable

    val categoriesLabelList = ExtraStore.Key<List<String>>()

    /**
     * Called when monthlyExpenses LiveData emits data.
     */
    fun processExpenses(items: List<ExpenseWithCategory>?) {
        if (!items.isNullOrEmpty()) {
            val groupedData = items.groupBy { it.category.name }
            val categoryNames = groupedData.keys.toList()
            val categorySums = groupedData.values.map { grp -> grp.sumOf { it.expense.amount } }

            _expenseData.value = categoryNames to categorySums
            updateChart(categoryNames, categorySums, _expenseChartModelProducer)
        } else {
            _expenseData.value = emptyList<String>() to emptyList()
        }

        // Check if BOTH expenses AND earnings exist
        checkIfDataExists()
    }

    /**
     * Called when monthlyEarnings LiveData emits data.
     */
    fun processEarnings(items: List<EarningWithCategory>?) {
        if (!items.isNullOrEmpty()) {
            val groupedData = items.groupBy { it.category.name }
            val categoryNames = groupedData.keys.toList()
            val categorySums = groupedData.values.map { grp -> grp.sumOf { it.earning.amount } }

            _earningData.value = categoryNames to categorySums
            updateChart(categoryNames, categorySums, _earningChartModelProducer)
        } else {
            _earningData.value = emptyList<String>() to emptyList()
        }

        // Check if BOTH expenses AND earnings exist
        checkIfDataExists()
    }

    /**
     * If either expenses or earnings is missing (empty),
     * we set _isDataAvailable to false. Otherwise true.
     */
    private fun checkIfDataExists() {
        val hasExpenseData = !_expenseData.value?.first.isNullOrEmpty()
        val hasEarningData = !_earningData.value?.first.isNullOrEmpty()

        // Show data ONLY if BOTH are present
        _isDataAvailable.value = hasExpenseData && hasEarningData
    }

    /**
     * Updates the chart with the given category names & sums.
     */
    private fun updateChart(
        categoryNames: List<String>,
        categorySums: List<Double>,
        chartModelProducer: CartesianChartModelProducer
    ) {
        if (categorySums.isEmpty()) return
        viewModelScope.launch {
            chartModelProducer.runTransaction {
                columnSeries { series(categorySums) }
                extras { extraStore ->
                    extraStore[categoriesLabelList] = categoryNames
                }
            }
        }
    }
}
