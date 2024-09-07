package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository

class CreateViewModelFactory(private val repository: FinancialRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
