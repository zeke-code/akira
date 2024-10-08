package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This class absolutely needs refactoring, as it is a mess.
 * At the moment, it works though.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinancialRepository
): ViewModel() {

    private val _allExpenses: LiveData<List<ExpenseWithCategory>> = repository.getAllExpensesWithCategory()
    val allExpenses: LiveData<List<ExpenseWithCategory>> get() = _allExpenses

    private val _allEarnings: LiveData<List<EarningWithCategory>> = repository.getAllEarningsWithCategory()
    val allEarnings: LiveData<List<EarningWithCategory>> get() = _allEarnings

    private val _expenseCategoryNames = MutableLiveData<List<String>>()
    val expenseCategoryNames: LiveData<List<String>> get() = _expenseCategoryNames

    private val _expenseCategorySums = MutableLiveData<List<Float>>()
    val expenseCategorySums: LiveData<List<Float>> get() = _expenseCategorySums

    private val _earningCategoryNames = MutableLiveData<List<String>>()
    val earningCategoryNames: LiveData<List<String>> get() = _earningCategoryNames

    private val _earningCategorySums = MutableLiveData<List<Float>>()
    val earningCategorySums: LiveData<List<Float>> get() = _earningCategorySums

    private val _expenseChartModelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    val expenseChartModelProducer: CartesianChartModelProducer get() = _expenseChartModelProducer

    private val _revenueChartModelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    val revenueChartModelProducer: CartesianChartModelProducer get() = _revenueChartModelProducer

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allExpenses.asFlow()
                .collectLatest { expenses ->
                    updateExpenseChart(expenses)
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            allEarnings.asFlow()
                .collectLatest { earnings ->
                    updateRevenueChart(earnings)
                }
        }
    }

    private suspend fun updateExpenseChart(expenses: List<ExpenseWithCategory>) {
        if (expenses.isEmpty()) {
            Log.w("StatsViewModel", "No expenses available to update the chart.")
            return
        }

        val groupedExpenses = expenses.groupBy { it.category.name }
        val categoryNames = groupedExpenses.keys.toList()
        val categorySums = groupedExpenses.values.map { group ->
            group.sumOf { it.expense.amount }.toFloat()
        }

        // Post values to LiveData
        _expenseCategoryNames.postValue(categoryNames)
        _expenseCategorySums.postValue(categorySums)

        // Update the chart model producer
        val labelListKey = ExtraStore.Key<List<String>>()
        _expenseChartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }
    }

    private suspend fun updateRevenueChart(earnings: List<EarningWithCategory>) {
        if (earnings.isEmpty()) {
            Log.w("StatsViewModel", "No earnings available to update the chart.")
            return
        }

        val groupedEarnings = earnings.groupBy { it.category.name }
        val categoryNames = groupedEarnings.keys.toList()
        val categorySums = groupedEarnings.values.map { group ->
            group.sumOf { it.earning.amount }.toFloat()
        }

        // Post values to LiveData
        _earningCategoryNames.postValue(categoryNames)
        _earningCategorySums.postValue(categorySums)

        // Update the revenue chart model producer
        val labelListKey = ExtraStore.Key<List<String>>()
        _revenueChartModelProducer.runTransaction {
            columnSeries { series(categorySums) }
            extras { extraStore ->
                extraStore[labelListKey] = categoryNames
            }
        }
    }
}
