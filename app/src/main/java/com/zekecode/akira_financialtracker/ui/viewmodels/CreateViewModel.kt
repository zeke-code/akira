package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateFragmentViewModel : ViewModel() {

    // LiveData for navigation
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    // Function to trigger navigation to HomeFragment
    fun onCancelClicked() {
        _navigateToHome.value = true
    }

    // Reset the navigation state
    fun doneNavigating() {
        _navigateToHome.value = false
    }
}