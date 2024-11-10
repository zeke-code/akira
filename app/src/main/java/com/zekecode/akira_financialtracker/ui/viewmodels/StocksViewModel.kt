package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val repository: StocksRepository
) : ViewModel() {

    private val _stockPrice = MutableStateFlow<String>("")
    val stockPrice: StateFlow<String> get() = _stockPrice

    private val _stockName = MutableStateFlow<String>("")
    val stockName: StateFlow<String> get() = _stockName
}