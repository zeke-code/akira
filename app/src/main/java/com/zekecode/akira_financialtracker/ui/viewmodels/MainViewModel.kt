package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isUpdateAvailable = MutableLiveData<Boolean>()
    val isUpdateAvailable: LiveData<Boolean> get() = _isUpdateAvailable

    init {
        checkForUpdates("zeke-code", "akira")
    }

    fun isSetupComplete(): Boolean {
        return userRepository.isSetupComplete()
    }

    /**
     * Function to update budget if last time app was launched was the previous month.
     * If user entered a new month, refresh his budget by adding an entry to the database
     * with the new month.
     */
    fun checkAndUpdateBudget() {
        val currentYearMonth = getCurrentYearMonth()
        if (userRepository.getLastLaunchDate() != currentYearMonth) {
            viewModelScope.launch {
                val currentBudget = userRepository.getBudget().toDouble()
                financialRepository.insertBudget(
                    BudgetModel(
                        yearMonth = currentYearMonth,
                        amount = currentBudget
                    )
                )
                userRepository.setLastLaunchDateToNow()
            }
        }
    }

    /**
     * Function to check for updates and return the APK URL if an update is available
     */
    private fun checkForUpdates(owner: String, repo: String) {
        viewModelScope.launch {
            val latestRelease = userRepository.fetchLatestRelease(owner, repo)
            if (latestRelease == null) {
                Log.d("MainViewModel", "No releases found or error fetching latest release.")
                _isUpdateAvailable.postValue(false)
                return@launch
            }
            Log.d("MainViewModel", "Latest release response is: ${latestRelease.toString()}")
            latestRelease.let {
                val latestVersionCode = it.tagName.removePrefix("v").toIntOrNull() ?: 0
                val currentVersionCode = userRepository.getAppVersionCode()

                if (latestVersionCode > currentVersionCode) {
                    _isUpdateAvailable.postValue(true)
                } else {
                    _isUpdateAvailable.postValue(false)
                }
            }
        }
    }
}