package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository

class HomeViewModel(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    init {
        fetchUserData()
        // Register the listener to listen for changes in SharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun fetchUserData() {
        _monthlyBudget.value = sharedPreferences.getFloat("MonthlyBudget", 0F)
    }

    // Correct implementation of onSharedPreferenceChanged
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        _monthlyBudget.value = sharedPreferences.getFloat(key, 0F)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
