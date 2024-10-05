package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("AkiraPrefs", Context.MODE_PRIVATE)

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _currencySymbol = MutableLiveData<String>()
    val currencySymbol: LiveData<String> get() = _currencySymbol

    private val _budget = MutableLiveData<Float?>()
    val budget: LiveData<Float?> get() = _budget

    // Holds both budget and currencySymbol to let UI update upon changes
    private val _combinedBudgetText = MediatorLiveData<String>().apply {
        addSource(budget) { value = combineBudgetAndSymbol() }
        addSource(_currencySymbol) { value = combineBudgetAndSymbol() }
    }

    val combinedBudgetText: LiveData<String> get() = _combinedBudgetText


    private fun combineBudgetAndSymbol(): String {
        val currentBudget = budget.value ?: 0F
        val currentSymbol = _currencySymbol.value ?: ""

        // Use application context to access the resources
        return getApplication<Application>().getString(R.string.settings_budget, currentBudget, currentSymbol)
    }


    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    private val _apiKey = MutableLiveData<String>()
    val apiKey: LiveData<String> get() = _apiKey

    init {
        loadSettings()
    }

    // Load settings asynchronously using viewModelScope
    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _username.postValue(sharedPreferences.getString("Username", "undefined"))
            _currencySymbol.postValue(CurrencyUtils.getCurrencySymbol(sharedPreferences))
            _budget.postValue(sharedPreferences.getFloat("MonthlyBudget", 0.0f))
            _notificationsEnabled.postValue(sharedPreferences.getBoolean("notifications_enabled", false))
            _selectedCurrency.postValue(sharedPreferences.getString("Currency", "USD"))
            _apiKey.postValue(sharedPreferences.getString("ApiKey", "none"))
        }
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                sharedPreferences.edit().putString("Username", newUsername).apply()
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
                sharedPreferences.edit().putFloat("MonthlyBudget", budgetValue).apply()
                _budget.postValue(budgetValue)
            }
        } else {
            Log.w("SettingsViewModel", "Invalid budget value: $newBudget")
        }
    }

    // Update notificationsEnabled asynchronously using viewModelScope
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
            _notificationsEnabled.postValue(enabled)
        }
    }

    fun updateCurrency(newCurrency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().putString("Currency", newCurrency).apply()
            _selectedCurrency.postValue(newCurrency)
        }
    }

    fun updateApiKey(newApiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().putString("ApiKey", newApiKey).apply()
            _apiKey.postValue(newApiKey)
        }
    }
}
