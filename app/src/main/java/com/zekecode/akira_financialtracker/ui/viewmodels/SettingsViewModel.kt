package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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

    private fun loadSettings() {
        _username.value = sharedPreferences.getString("Username", "undefined")
        _budget.value = sharedPreferences.getFloat("MonthlyBudget", 0.0f)
        _notificationsEnabled.value = sharedPreferences.getBoolean("notifications_enabled", false)
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            sharedPreferences.edit().putString("Username", newUsername).apply()
            _username.value = newUsername // Update the LiveData directly
        } else {
            Log.w("SettingsViewModel", "Attempted to set an empty username.")
        }
    }

    fun updateBudget(newBudget: String) {
        val budgetValue = newBudget.toFloatOrNull()
        if (budgetValue != null && budgetValue >= 0) {
            sharedPreferences.edit().putFloat("MonthlyBudget", budgetValue).apply()
            _budget.value = budgetValue
        } else {
            Log.w("SettingsViewModel", "Invalid budget value: $newBudget")
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }
}
