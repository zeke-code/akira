package com.zekecode.akira_financialtracker.ui.viewmodels

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
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val currentYearMonth = getCurrentYearMonth()

    private val monthlyExpenses: LiveData<List<ExpenseWithCategory>> =
        repository.getMonthlyExpenses(currentYearMonth)
    private val monthlyEarnings: LiveData<List<EarningWithCategory>> =
        repository.getMonthlyEarnings(currentYearMonth)

    private val _expenseChartModelProducer = CartesianChartModelProducer()
    val expenseChartModelProducer: CartesianChartModelProducer
        get() = _expenseChartModelProducer

    private val _earningChartModelProducer = CartesianChartModelProducer()
    val earningChartModelProducer: CartesianChartModelProducer
        get() = _earningChartModelProducer

    private val _expenseData = MutableLiveData<Pair<List<String>, List<Double>>>()
    val expenseData: LiveData<Pair<List<String>, List<Double>>>
        get() = _expenseData

    private val _earningData = MutableLiveData<Pair<List<String>, List<Double>>>()
    val earningData: LiveData<Pair<List<String>, List<Double>>>
        get() = _earningData

    val labelListKey = ExtraStore.Key<List<String>>()

    init {
        processFinancialData(
            data = monthlyExpenses,
            getCategoryName = { it.category.name },
            getAmount = { it.expense.amount },
            onProcessed = { names, sums ->
                _expenseData.value = names to sums
                updateChart(
                    categoryNames = names,
                    categorySums = sums,
                    chartModelProducer = _expenseChartModelProducer
                )
            }
        )

        processFinancialData(
            data = monthlyEarnings,
            getCategoryName = { it.category.name },
            getAmount = { it.earning.amount },
            onProcessed = { names, sums ->
                _earningData.value = names to sums
                updateChart(
                    categoryNames = names,
                    categorySums = sums,
                    chartModelProducer = _earningChartModelProducer
                )
            }
        )
    }

    /**
     * A generic function to transform the data (List<T>) into names and sums.
     */
    private fun <T> processFinancialData(
        data: LiveData<List<T>>,
        getCategoryName: (T) -> String,
        getAmount: (T) -> Double,
        onProcessed: (List<String>, List<Double>) -> Unit
    ) {
        data.observeForever { items ->
            if (!items.isNullOrEmpty()) {
                val groupedData = items.groupBy(getCategoryName)
                val categoryNames = groupedData.keys.toList()
                val categorySums = groupedData.values.map { group -> group.sumOf(getAmount) }
                onProcessed(categoryNames, categorySums)
            } else {
                onProcessed(emptyList(), emptyList())
            }
        }
    }

    /**
     * Updates the [chartModelProducer] with new data
     */
    private fun updateChart(
        categoryNames: List<String>,
        categorySums: List<Double>,
        chartModelProducer: CartesianChartModelProducer
    ) {
        if (categorySums.isEmpty()) {
            return
        }
        else{
            viewModelScope.launch {
                chartModelProducer.runTransaction {
                    columnSeries { series(categorySums) }
                    extras { extraStore ->
                        extraStore[labelListKey] = categoryNames
                    }
                }
            }
        }
    }
}
