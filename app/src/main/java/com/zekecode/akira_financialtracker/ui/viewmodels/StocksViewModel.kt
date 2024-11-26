package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.data.remote.models.DailyData
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
    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Double>>> = _chartData

    private val _stockName = MutableLiveData<String?>()
    val stockName: LiveData<String?> get() = _stockName

    private val _stockPrice = MutableLiveData<String?>()
    val stockPrice: LiveData<String?> get() = _stockPrice

    private val _isApiKeyPresent = MutableLiveData<Boolean>()
    val isApiKeyPresent: LiveData<Boolean> get() = _isApiKeyPresent

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModelScope.launch {
            userRepository.apiKeyFlow.collect { apiKey ->
                _isApiKeyPresent.value = isApiKeyValid(apiKey)
            }
        }
    }

    fun fetchStockData(symbol: String) {
        val apiKey = userRepository.getApiKey()
        if (isApiKeyValid(apiKey)) {
            viewModelScope.launch {
                val dailyTimeSeriesData = stocksRepository.getDailyTimeSeries(symbol, apiKey)
                val globalQuoteData = stocksRepository.getStockQuote(symbol, apiKey)
                dailyTimeSeriesData.onSuccess { data ->
                    _stockName.value = data.metaData.symbol
                    val chartData = extractChartData(data.timeSeries)
                    _chartData.value = chartData
                    Log.d("StocksViewModel", "Response data is: $data")
                }.onFailure { error ->
                    _errorMessage.value = "Could not retrieve stock information... try again later."
                    Log.d("StocksViewModel", "$error")
                }
                globalQuoteData.onSuccess { data ->
                    _stockPrice.value = data.globalQuote.price
                    Log.d("StocksViewModel", "Response data is: $data")
                }.onFailure { error ->
                    Log.d("StocksViewModel", "$error")
                }
            }
        }
    }

    private fun extractChartData(timeSeries: Map<String, DailyData>): List<Pair<String, Double>> {
        return timeSeries.entries
            .mapNotNull { entry ->
                val date = entry.key
                val closePrice = entry.value.close.toDoubleOrNull()
                if (closePrice != null) {
                    date to closePrice
                } else null
            }
            .sortedBy { it.first }
    }

    private fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && apiKey != "none"
    }
}
