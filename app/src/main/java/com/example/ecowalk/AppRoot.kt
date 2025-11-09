package com.example.ecowalk.ui

import androidx.compose.runtime.*
import com.example.ecowalk.data.local.User
import com.example.ecowalk.ui.auth.AuthScreen
import com.example.ecowalk.ui.home.HomeScreen

@Composable
fun AppRoot() {
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    if (loggedInUser == null) {
        AuthScreen(onAuthSuccess = { user -> loggedInUser = user })
    } else {
        HomeScreen(user = loggedInUser!!, onLogout = { loggedInUser = null })
    }
}
