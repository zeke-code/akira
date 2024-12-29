package com.zekecode.akira_financialtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.data.remote.models.DailyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val stocksRepository: StocksRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())

    private val _stocksChartModelProducer = CartesianChartModelProducer()
    val stocksChartModelProducer: CartesianChartModelProducer get() = _stocksChartModelProducer

    private val _stockName = MutableLiveData<String?>()
    val stockName: LiveData<String?> get() = _stockName

    private val _stockPrice = MutableLiveData<String?>()
    val stockPrice: LiveData<String?> get() = _stockPrice

    private val _isApiKeyValid = MutableLiveData<Boolean>()
    val isApiKeyValid: LiveData<Boolean> get() = _isApiKeyValid

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Label key for chart axis
    val dateLabels = ExtraStore.Key<List<String>>()

    init {
        observeApiKey()
    }

    private fun observeApiKey() {
        viewModelScope.launch {
            userRepository.apiKeyFlow.collect { apiKey ->
                _isApiKeyValid.value = isApiKeyValid(apiKey)
            }
        }
    }

    fun fetchStockData(symbol: String) {
        val apiKey = userRepository.getApiKey()
        if (isApiKeyValid(apiKey)) {
            viewModelScope.launch {
                val dailyTimeSeriesResult = stocksRepository.getDailyTimeSeries(symbol, apiKey)
                val globalQuoteResult = stocksRepository.getStockQuote(symbol, apiKey)

                dailyTimeSeriesResult.onSuccess { data ->
                    _stockName.value = data.metaData.symbol
                    val chartData = extractChartData(data.timeSeries)
                    _chartData.value = chartData
                    updateChart(chartData)
                }.onFailure {
                    _errorMessage.value = "Failed to retrieve stock data. Please try again."
                }

                globalQuoteResult.onSuccess { data ->
                    _stockPrice.value = data.globalQuote.price
                }.onFailure {
                    Log.e("StocksViewModel", "Failed to fetch global quote: $it")
                }
            }
        } else {
            _errorMessage.value = "Invalid API key. Please correct it in the settings."
        }
    }

    private fun extractChartData(timeSeries: Map<String, DailyData>): List<Pair<String, Double>> {
        return timeSeries.entries
            .sortedBy { it.key }
            .mapNotNull { entry ->
                val date = entry.key
                val closePrice = entry.value.close.toDoubleOrNull()
                if (closePrice != null) {
                    date to closePrice
                } else null
            }
    }

    private fun updateChart(chartData: List<Pair<String, Double>>) {
        val dates = chartData.map { it.first }
        val prices = chartData.map { it.second }

        viewModelScope.launch {
            _stocksChartModelProducer.runTransaction {
                lineSeries {
                    series(prices)
                }
                extras { extraStore ->
                    extraStore[dateLabels] = dates
                }
            }
        }
    }

    private fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && apiKey != "none"
    }
}
