package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.launch

class CreateViewModel(private val repository: FinancialRepository) : ViewModel() {

    // LiveData for navigation
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    // LiveData to handle user input
    private val _amount = MutableLiveData<String>()
    val amount: LiveData<String>
        get() = _amount

    init {

    }

    // Function to trigger navigation to HomeFragment
    fun onCancelClicked() {
        _navigateToHome.value = true
    }

    // Reset the navigation state
    fun doneNavigating() {
        _navigateToHome.value = false
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    // Function to insert a new earning
    fun insertEarning(name: String, category: String, date: Long) {
        val amountValue = _amount.value?.toDoubleOrNull() ?: 0.0 // Convert amount to Double
        val earning = EarningModel(name = name, amount = amountValue, category = category, date = date)
        viewModelScope.launch {
            repository.insertEarning(earning)
            _navigateToHome.value = true
        }
    }

}
