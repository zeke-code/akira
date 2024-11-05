package com.zekecode.akira_financialtracker.data.local.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    private val _currencySymbol = MutableLiveData<String>()
    private val dateFormat = getCurrentYearMonth()
    val currencySymbolLiveData: LiveData<String> get() = _currencySymbol

    init {
        loadCurrencySymbol()
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "Currency") {
                loadCurrencySymbol()
            }
        }
    }

    private fun loadCurrencySymbol() {
        _currencySymbol.value = CurrencyUtils.getCurrencySymbol(sharedPreferences)
    }

    fun isSetupComplete(): Boolean {
        return sharedPreferences.getBoolean("IsSetupComplete", false)
    }

    fun saveUserData(userName: String, monthlyBudget: Float, selectedCurrency: String) {
        sharedPreferences.edit().apply {
            putString("Username", userName)
            putFloat("MonthlyBudget", monthlyBudget)
            putString("Currency", selectedCurrency)
            putString("ApiKey", "")
            putBoolean("IsSetupComplete", true)
            apply()
        }
    }

    fun getUsername(): String = sharedPreferences.getString("Username", "undefined") ?: "undefined"
    fun getBudget(): Float = sharedPreferences.getFloat("MonthlyBudget", 0.0f)
    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", false)
    fun getSelectedCurrency(): String = sharedPreferences.getString("Currency", "USD") ?: "USD"
    fun getApiKey(): String = sharedPreferences.getString("ApiKey", "none") ?: "none"
    fun getCurrencySymbol(): String {
        return CurrencyUtils.getCurrencySymbol(sharedPreferences)
    }

    fun updateUsername(newUsername: String) {
        sharedPreferences.edit().putString("Username", newUsername).apply()
    }

    fun updateBudget(newBudget: Float) {
        sharedPreferences.edit().putFloat("MonthlyBudget", newBudget).apply()
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun updateSelectedCurrency(newCurrency: String) {
        sharedPreferences.edit().putString("Currency", newCurrency).apply()
    }

    fun updateApiKey(newApiKey: String) {
        sharedPreferences.edit().putString("ApiKey", newApiKey).apply()
    }

    fun getLastLaunchDate(): String? {
        return sharedPreferences.getString("lastLaunchDate", null)
    }

    fun setLastLaunchDate(date: String) {
        sharedPreferences.edit().putString("lastLaunchDate", date).apply()
    }

    fun setLastLaunchDateToNow() {
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        setLastLaunchDate(currentDate)
    }
}
