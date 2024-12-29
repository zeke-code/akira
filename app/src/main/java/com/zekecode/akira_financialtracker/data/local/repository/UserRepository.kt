package com.zekecode.akira_financialtracker.data.local.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zekecode.akira_financialtracker.data.remote.api.GithubApiService
import com.zekecode.akira_financialtracker.data.remote.models.GitHubRelease
import com.zekecode.akira_financialtracker.utils.CurrencyUtils
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val currencyUtils: CurrencyUtils,
    private val githubApiService: GithubApiService,
    private val context: Context
) {

    private val _currencySymbol = MutableLiveData<String>()
    private val dateFormat = getCurrentYearMonth()

    // Used to expose API key changes as a Flow
    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow = _apiKeyFlow.asStateFlow()

    // Exposes the current currency symbol
    val currencySymbolLiveData: LiveData<String> get() = _currencySymbol

    init {
        loadCurrencySymbol()

        // Register listener to detect changes in SharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "Currency" -> {
                    loadCurrencySymbol()
                }
                "ApiKey" -> {
                    _apiKeyFlow.value = getApiKey()
                }
            }
        }
    }

    /**
     * Loads the currency symbol from the repository’s selected currency code
     * and updates the LiveData value.
     */
    private fun loadCurrencySymbol() {
        val currentCode = getSelectedCurrency()
        _currencySymbol.value = currencyUtils.getCurrencySymbol(currentCode)
    }

    /**
     * Formats the amount with the current user-selected currency.
     */
    fun getCurrencySymbol(amount: Double): String {
        val currencyCode = getSelectedCurrency()
        return currencyUtils.formatAmountWithCurrency(amount, currencyCode)
    }

    /**
     * Checks whether the initial setup is complete.
     */
    fun isSetupComplete(): Boolean {
        return sharedPreferences.getBoolean("IsSetupComplete", false)
    }

    /**
     * Saves user data (username, monthly budget, selected currency) in SharedPreferences.
     */
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

    // Basic getters
    fun getUsername(): String = sharedPreferences.getString("Username", "undefined") ?: "undefined"
    fun getBudget(): Float = sharedPreferences.getFloat("MonthlyBudget", 0.0f)
    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", false)
    fun getSelectedCurrency(): String = sharedPreferences.getString("Currency", "USD") ?: "USD"
    fun getApiKey(): String = sharedPreferences.getString("ApiKey", "none") ?: "none"

    /**
     * Returns the current currency symbol (not the formatted amount).
     */
    fun getCurrencySymbol(): String {
        val currencyCode = getSelectedCurrency()
        return currencyUtils.getCurrencySymbol(currencyCode)
    }

    // Basic update methods
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

    // Managing the last launch date for analytics or tracking
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

    // Retrieves the application's version name
    fun getAppVersionName(): String? {
        val packageManager = context.packageManager
        val packageName = context.packageName
        return packageManager.getPackageInfo(packageName, 0).versionName
    }

    /** Methods related to GitHub API **/
    suspend fun fetchLatestRelease(): GitHubRelease? {
        return try {
            githubApiService.getLatestRelease()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}