package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.repository.SharedPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val application: Application
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _currencySymbol = MutableLiveData<String>()
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _budget = MutableLiveData<Float?>()
    val budget: LiveData<Float?> get() = _budget

    private val _combinedBudgetText = MediatorLiveData<String>().apply {
        addSource(budget) { value = combineBudgetAndSymbol() }
        addSource(_currencySymbol) { value = combineBudgetAndSymbol() }
    }
    val combinedBudgetText: LiveData<String> get() = _combinedBudgetText

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    private val _apiKey = MutableLiveData<String>()
    val apiKey: LiveData<String> get() = _apiKey

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _username.postValue(sharedPreferencesRepository.getUsername())
            _currencySymbol.postValue(sharedPreferencesRepository.getCurrencySymbol())
            _budget.postValue(sharedPreferencesRepository.getBudget())
            _notificationsEnabled.postValue(sharedPreferencesRepository.isNotificationsEnabled())
            _selectedCurrency.postValue(sharedPreferencesRepository.getSelectedCurrency())
            _apiKey.postValue(sharedPreferencesRepository.getApiKey())
        }
    }

    private fun combineBudgetAndSymbol(): String {
        val currentBudget = budget.value ?: 0F
        val currentSymbol = _currencySymbol.value ?: ""
        return application.getString(R.string.settings_budget, currentBudget, currentSymbol)
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                sharedPreferencesRepository.updateUsername(newUsername)
                _username.postValue(newUsername)
            }
        } else {
            Log.w("SettingsViewModel", "Attempted to set an empty username.")
        }
    }

    fun updateBudget(newBudget: String) {
        val budgetValue = newBudget.toFloatOrNull()
        if (budgetValue != null && budgetValue >= 0) {
            viewModelScope.launch(Dispatchers.IO) {
                sharedPreferencesRepository.updateBudget(budgetValue)
                _budget.postValue(budgetValue)
            }
        } else {
            Log.w("SettingsViewModel", "Invalid budget value: $newBudget")
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferencesRepository.updateNotificationsEnabled(enabled)
            _notificationsEnabled.postValue(enabled)
        }
    }

    fun updateCurrency(newCurrency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferencesRepository.updateSelectedCurrency(newCurrency)
            _selectedCurrency.postValue(newCurrency)
        }
    }

    fun updateApiKey(newApiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferencesRepository.updateApiKey(newApiKey)
            _apiKey.postValue(newApiKey)
        }
    }
}
