package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.*
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    private val _amount = MutableLiveData<Double>()
    val amount: LiveData<Double>
        get() = _amount

    val allCategories: LiveData<List<CategoryModel>> = repository.getAllCategories()

    private val _selectedCategory = MutableLiveData<CategoryModel?>()
    val selectedCategory: LiveData<CategoryModel?> get() = _selectedCategory

    private val _selectedDate = MutableLiveData<Long>()
    val selectedDate: LiveData<Long> get() = _selectedDate

    val formattedSelectedDate: LiveData<String> = _selectedDate.map { dateInMillis ->
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.format(Date(dateInMillis))
    }

    private val _description = MutableLiveData<String>()

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

    fun setTransactionDescription(value: String) {
        _description.value = value
    }

    fun setIsExpense(isExpense: Boolean) {
        _isExpense.value = isExpense
    }

    // Reset the navigation state
    fun doneNavigating() {
        _navigateToHome.value = false
    }

    fun insertTransaction(isExpense: Boolean) {
        val amountValue = _amount.value ?: 0.0
        val descriptionValue = _description.value
        val categoryValue = _selectedCategory.value
        val dateValue = _selectedDate.value ?: System.currentTimeMillis()

        if (categoryValue != null) {
            viewModelScope.launch {
                if (isExpense) {
                    val expense = ExpenseModel(
                        amount = amountValue,
                        categoryId = categoryValue.id,
                        date = dateValue,
                        description = descriptionValue
                    )
                    repository.insertExpense(expense)
                } else {
                    val earning = EarningModel(
                        amount = amountValue,
                        categoryId = categoryValue.id,
                        date = dateValue,
                        description = descriptionValue
                    )
                    repository.insertEarning(earning)
                }
                resetData()
                _navigateToHome.postValue(true)
            }
        }
    }

    // Function to validate data before creating a new transaction
    fun canInsertTransaction(): Boolean {
        val amountValue = _amount.value ?: 0.0
        return amountValue > 0 && _selectedCategory.value != null && _selectedDate.value != null
    }

    // Function needed to clear data if user changes view.
    private fun resetData() {
        _description.value = ""
        _selectedCategory.value = null
        _selectedDate.value = System.currentTimeMillis()
        _isExpense.value = true
    }
}
