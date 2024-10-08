package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FirstSetupViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isSetupComplete = MutableLiveData<Boolean>()
    val isSetupComplete: LiveData<Boolean> get() = _isSetupComplete

    private val _showReadyView = MutableLiveData<Boolean>()
    val showReadyView: LiveData<Boolean> get() = _showReadyView

    fun saveUserData(userName: String, monthlyBudgetStr: String, selectedCurrency: String) {
        try {
            val monthlyBudget = monthlyBudgetStr.toFloat()
            if (!isValidDecimal(monthlyBudgetStr) || monthlyBudget < 20) {  // Note: Fixed `&&` to `||`
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
                    delay(2000)  // Optional delay for animation
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
