package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("AkiraPrefs", Application.MODE_PRIVATE)

    private val _isSetupComplete = MutableLiveData<Boolean>()
    val isSetupComplete: LiveData<Boolean> get() = _isSetupComplete

    private val _showReadyView = MutableLiveData<Boolean>()
    val showReadyView: LiveData<Boolean> get() = _showReadyView

    fun saveUserData(userName: String, monthlyBudgetStr: String, selectedCurrency: String) {
        try {
            val monthlyBudget = monthlyBudgetStr.toFloat()
            if (!isValidDecimal(monthlyBudgetStr) && monthlyBudget < 20) {
                _showReadyView.value = false
            } else {
                _showReadyView.value = true
                viewModelScope.launch {
                    // Save data in SharedPreferences
                    withContext(Dispatchers.IO) {
                        sharedPreferences.edit().apply {
                            putString("Username", userName)
                            putFloat("MonthlyBudget", monthlyBudget)
                            putString("Currency", selectedCurrency)
                            putString("ApiKey", "")
                            putBoolean("IsSetupComplete", true)
                            apply()
                        }
                    }
                    delay(4000)
                    _isSetupComplete.value = true // Signal to Activity to switch to MainActivity
                }
            }
        } catch (e: NumberFormatException) {
            _showReadyView.value = false
        }
    }

    private fun isValidDecimal(numberStr: String): Boolean {
        val parts = numberStr.split(".")
        return parts.size <= 1 || parts[1].length <= 2
    }
}
