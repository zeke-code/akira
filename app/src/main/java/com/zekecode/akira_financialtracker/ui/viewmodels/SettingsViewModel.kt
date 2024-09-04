package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("AkiraPrefs", Context.MODE_PRIVATE)

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _budget = MutableLiveData<Float?>()
    val budget: LiveData<Float?> get() = _budget

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    init {
        loadSettings()
    }

    // Load settings asynchronously using viewModelScope
    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _username.postValue(sharedPreferences.getString("Username", "undefined"))
            _budget.postValue(sharedPreferences.getFloat("MonthlyBudget", 0.0f))
            _notificationsEnabled.postValue(sharedPreferences.getBoolean("notifications_enabled", false))
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
}
