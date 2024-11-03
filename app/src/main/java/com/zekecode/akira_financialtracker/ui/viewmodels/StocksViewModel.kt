package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val repository: StocksRepository
) : ViewModel() {
}