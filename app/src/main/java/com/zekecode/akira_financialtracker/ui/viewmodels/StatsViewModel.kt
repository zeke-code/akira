package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
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

/**
 * This class needs some refactoring, as right now it's total garbage.
 * But hey, it works! I hate vico charts.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val currentYearMonth = getCurrentYearMonth()

    private val monthlyExpenses: LiveData<List<ExpenseWithCategory>> =
        repository.getMonthlyExpenses(currentYearMonth)
    private val monthlyEarnings: LiveData<List<EarningWithCategory>> =
        repository.getMonthlyEarnings(currentYearMonth)

    private val _expenseCategoryNames = MediatorLiveData<List<String>>()
    val expenseCategoryNames: LiveData<List<String>> get() = _expenseCategoryNames

    private val _expenseCategorySums = MediatorLiveData<List<Float>>()

    private val _earningCategoryNames = MediatorLiveData<List<String>>()
    val earningCategoryNames: LiveData<List<String>> get() = _earningCategoryNames

    private val _earningCategorySums = MediatorLiveData<List<Float>>()

    private val _expenseChartModelProducer = CartesianChartModelProducer()
    val expenseChartModelProducer: CartesianChartModelProducer get() = _expenseChartModelProducer

    private val _earningChartModelProducer = CartesianChartModelProducer()
    val earningChartModelProducer: CartesianChartModelProducer get() = _earningChartModelProducer

    private val _isEarningDataEmpty = MutableLiveData<Boolean>()
    val isEarningDataEmpty: LiveData<Boolean> get() = _isEarningDataEmpty

    private val _isExpenseDataEmpty = MutableLiveData<Boolean>()
    val isExpenseDataEmpty: LiveData<Boolean> get() = _isExpenseDataEmpty

    init {
        _expenseCategoryNames.addSource(monthlyExpenses) { expenses ->
            updateExpenseData(expenses)
        }

        _expenseCategorySums.addSource(monthlyExpenses) { expenses ->
            updateExpenseData(expenses)
        }

        _earningCategoryNames.addSource(monthlyEarnings) { earnings ->
            updateEarningsData(earnings)
        }

        _earningCategorySums.addSource(monthlyEarnings) {earnings ->
            updateEarningsData(earnings)
        }
    }

    private fun updateExpenseData(expenses: List<ExpenseWithCategory>) {
        if (expenses.isEmpty()) {
            Log.w("StatsViewModel", "No expenses available to update the data.")
            _isExpenseDataEmpty.value = true
            return
        }

        val groupedExpenses = expenses.groupBy { it.category.name }
        val categoryNames = groupedExpenses.keys.toList()
        val categorySums = groupedExpenses.values.map { group ->
            group.sumOf { it.expense.amount }.toFloat()
        }

        _expenseCategoryNames.value = categoryNames
        _expenseCategorySums.value = categorySums

        viewModelScope.launch {
            updateExpenseChart(categoryNames, categorySums)
        }
    }

    private fun updateEarningsData(earnings: List<EarningWithCategory>) {
        if (earnings.isEmpty()) {
            Log.w("StatsViewModel", "No earnings available to update the data.")
            _isEarningDataEmpty.value = true
            return
        }

        val groupedEarnings = earnings.groupBy { it.category.name }
        val categoryNames = groupedEarnings.keys.toList()
        val categorySums = groupedEarnings.values.map { group ->
            group.sumOf { it.earning.amount }.toFloat()
        }

        _earningCategoryNames.value = categoryNames
        _earningCategorySums.value = categorySums

        viewModelScope.launch {
            updateEarningsChart(categoryNames, categorySums)
        }
    }

    private suspend fun updateExpenseChart(categoryNames: List<String>, categorySums: List<Float>) {
        val labelListKey = ExtraStore.Key<List<String>>()
        _expenseChartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }
    }

    private suspend fun updateEarningsChart(categoryNames: List<String>, categorySums: List<Float>) {
        val labelListKey = ExtraStore.Key<List<String>>()
        _earningChartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }
    }
}
