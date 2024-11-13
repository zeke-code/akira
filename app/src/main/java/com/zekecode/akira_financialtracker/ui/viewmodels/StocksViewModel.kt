package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.repository.StocksRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val stocksRepository: StocksRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _stockPrice = MutableStateFlow<String>("")
    val stockPrice: StateFlow<String> get() = _stockPrice

    private val _stockName = MutableStateFlow<String>("")
    val stockName: StateFlow<String> get() = _stockName

    private val _isApiKeyPresent = MutableLiveData<Boolean>()
    val isApiKeyPresent: LiveData<Boolean> get() = _isApiKeyPresent

    init {
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModelScope.launch {
            userRepository.apiKeyFlow.collect { apiKey ->
                _isApiKeyPresent.value = apiKey.isNotEmpty() && apiKey != "none"
            }
        }
    }
}
