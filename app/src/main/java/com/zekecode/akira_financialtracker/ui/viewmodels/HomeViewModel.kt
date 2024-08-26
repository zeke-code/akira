package com.zekecode.akira_financialtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinancialRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _monthlyBudget = MutableLiveData<Float>()
    val monthlyBudget: LiveData<Float> get() = _monthlyBudget

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        _monthlyBudget.value = sharedPreferences.getFloat("MonthlyBudget", 0F)
    }
}
