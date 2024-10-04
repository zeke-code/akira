package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateViewModel(private val repository: FinancialRepository) : ViewModel() {

    // LiveData for navigation
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    // LiveData to handle user input
    private val _amount = MutableLiveData<Double>()
    val amount: LiveData<Double>
        get() = _amount

    // LiveData for all categories
    val allCategories: LiveData<List<CategoryModel>> = repository.allCategories

    // LiveData for the selected category
    private val _selectedCategory = MutableLiveData<CategoryModel?>()
    val selectedCategory: LiveData<CategoryModel?> get() = _selectedCategory

    private val _selectedDate = MutableLiveData<Long>()
    val selectedDate: LiveData<Long> get() = _selectedDate

    val formattedSelectedDate: LiveData<String> = _selectedDate.map { dateInMillis ->
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.format(Date(dateInMillis))
    }

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _isExpense = MutableLiveData<Boolean>()
    val isExpense: LiveData<Boolean> get() = _isExpense

    init {
        resetData()
        _selectedDate.value = System.currentTimeMillis()
    }

    fun setAmount(value: Double) {
        _amount.value = value
    }

    fun setSelectedCategory(category: CategoryModel) {
        _selectedCategory.value = category
    }

    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    fun setTransactionName(value: String) {
        _name.value = value
    }

    fun setIsExpense(isExpense: Boolean) {
        _isExpense.value = isExpense
    }

    // Reset the navigation state
    fun doneNavigating() {
        _navigateToHome.value = false
    }

    // Function to insert a new earning
    fun insertEarning() {
        val amountValue = _amount.value ?: 0.0
        val nameValue = _name.value ?: ""
        val categoryValue = _selectedCategory.value
        val dateValue = _selectedDate.value ?: System.currentTimeMillis()

        if (categoryValue != null) {
            val earning = EarningModel(
                name = nameValue,
                amount = amountValue,
                categoryId = categoryValue.id,
                date = dateValue
            )
            viewModelScope.launch {
                repository.insertEarning(earning)
                resetData()
                _navigateToHome.postValue(true)
            }
        }
    }

    fun insertExpense() {
        val amountValue = _amount.value ?: 0.0
        val nameValue = _name.value ?: ""
        val categoryValue = _selectedCategory.value
        val dateValue = _selectedDate.value ?: System.currentTimeMillis()

        if (categoryValue != null) {
            val expense = ExpenseModel(
                name = nameValue,
                amount = amountValue,
                categoryId = categoryValue.id,
                date = dateValue
            )
            viewModelScope.launch {
                repository.insertExpense(expense)
                resetData()
                _navigateToHome.postValue(true)
            }
        }
    }

    // Function to reset ViewModel's data to null.
    private fun resetData() {
        _name.value = ""
        _selectedCategory.value = null
        _selectedDate.value = System.currentTimeMillis() // Reset time to user's current day
        _isExpense.value = true
    }
}
