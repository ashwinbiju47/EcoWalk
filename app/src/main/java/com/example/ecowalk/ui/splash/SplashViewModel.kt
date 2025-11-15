package com.example.ecowalk.ui.splash

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        // Example: check login state from local database or shared preferences
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        // TODO: Replace with actual check (repository or DataStore)
        _isUserLoggedIn.value = false
    }
}
