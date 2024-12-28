package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userRepository: UserRepository,
    private val currencyUtils: CurrencyUtils
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> get() = _appVersion

    private val _currencySymbol = MutableLiveData<String>()

    private val _budget = MutableLiveData<Float?>()
    val budget: LiveData<Float?> get() = _budget

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    private val _apiKey = MutableLiveData<String>()
    val apiKey: LiveData<String> get() = _apiKey

    private val _invalidInputToastText = MutableLiveData<String>()
    val invalidInputToastText: LiveData<String> get() = _invalidInputToastText

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _username.postValue(userRepository.getUsername())
            _currencySymbol.postValue(userRepository.getCurrencySymbol())
            _budget.postValue(userRepository.getBudget().toFloat())
            _notificationsEnabled.postValue(userRepository.isNotificationsEnabled())
            _selectedCurrency.postValue(userRepository.getSelectedCurrency())
            _apiKey.postValue(userRepository.getApiKey())
            _appVersion.postValue(userRepository.getAppVersionName())
        }
    }


    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.updateUsername(newUsername)
                _username.postValue(newUsername)
            }
        } else {
            _invalidInputToastText.value = "Username cannot be empty."
        }
    }

    fun updateBudget(newBudget: String) {
        val budgetValue = newBudget.toDoubleOrNull()
        val displayBudget = newBudget.toFloatOrNull()
        if (budgetValue != null && budgetValue > 0 && displayBudget != null) {
            val currentYearMonth = getCurrentYearMonth()

            viewModelScope.launch(Dispatchers.IO) {
                val budgetModel = BudgetModel(
                    yearMonth = currentYearMonth,
                    amount = budgetValue
                )
                financialRepository.insertBudget(budgetModel)
                userRepository.updateBudget(displayBudget)
                _budget.postValue(displayBudget)
            }
        } else {
            _invalidInputToastText.postValue("Budget cannot be empty and must be greater than 0.")
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateNotificationsEnabled(enabled)
            _notificationsEnabled.postValue(enabled)
        }
    }

    fun updateCurrency(newCurrency: String) {
        _selectedCurrency.value = newCurrency
        val newSymbol = currencyUtils.getCurrencySymbol(newCurrency)
        _currencySymbol.value = newSymbol

        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateSelectedCurrency(newCurrency)
        }
    }

    fun updateApiKey(newApiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateApiKey(newApiKey)
            _apiKey.postValue(newApiKey)
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            financialRepository.deleteAllTransactions()
        }
    }
}
