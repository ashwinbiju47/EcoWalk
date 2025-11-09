package com.example.ecowalk.ui.auth

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowalk.data.local.User
import com.example.ecowalk.data.local.UserDatabase
import com.example.ecowalk.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = UserDatabase.getDatabase(application).userDao()
    private val repository = UserRepository(userDao)

    var authMessage = mutableStateOf<String?>(null)
        private set

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val success = repository.registerUser(email, password)
            authMessage.value = if (success) "Account created successfully!" else "User already exists."
        }
    }

    fun login(email: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) {
                onSuccess(user)
            } else {
                authMessage.value = "Invalid email or password."
            }
        }
    }
}
