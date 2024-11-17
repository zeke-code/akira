package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.data.remote.models.TimeSeriesDailyModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val stocksRepository: StocksRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _stockData = MutableStateFlow<TimeSeriesDailyModel?>(null)
    val stockData: StateFlow<TimeSeriesDailyModel?> = _stockData

    private val _isApiKeyPresent = MutableLiveData<Boolean>()
    val isApiKeyPresent: LiveData<Boolean> get() = _isApiKeyPresent

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        setUpObservers()
        if (_isApiKeyPresent.value != null) { fetchStockData("AAPL", userRepository.getApiKey()) }
    }

    private fun setUpObservers() {
        viewModelScope.launch {
            userRepository.apiKeyFlow.collect { apiKey ->
                _isApiKeyPresent.value = isApiKeyValid(apiKey)
            }
        }
    }

    fun fetchStockData(symbol: String, apiKey: String) {
        viewModelScope.launch {
            val result = stocksRepository.getDailyTimeSeries(symbol, apiKey)
            result.onSuccess { data ->
                _stockData.value = data
                Log.d("StocksViewModel", "Response data is: $data")
            }.onFailure { error ->
                _errorMessage.value = "Could not retrieve stock information... try again later."
                Log.d("StocksViewModel", "Error, response data is: $error")
            }
        }
    }

    private fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && apiKey != "none"
    }

}
