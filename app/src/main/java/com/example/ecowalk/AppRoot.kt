package com.example.ecowalk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowalk.data.local.User
import com.example.ecowalk.data.local.UserDatabase
import com.example.ecowalk.data.repository.GreenWalkRepository
import com.example.ecowalk.network.RetrofitClient
import com.example.ecowalk.ui.auth.AuthScreen
import com.example.ecowalk.ui.auth.AuthViewModel
import com.example.ecowalk.ui.greenwalk.GreenWalkScreen
import com.example.ecowalk.ui.greenwalk.GreenWalkViewModel
import com.example.ecowalk.ui.greenwalk.GreenWalkViewModelFactory
import com.example.ecowalk.ui.home.HomeScreen
import com.example.ecowalk.ui.splash.SplashScreen
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel()

    val auth = FirebaseAuth.getInstance()
    var loggedInUser by remember { mutableStateOf<User?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    var showGreenWalk by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        delay(300)
        showSplash = false

        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val localUser = viewModel.getLocalUser(firebaseUser.uid)
            loggedInUser = localUser
        }
    }

    AnimatedVisibility(
        visible = showSplash,
        exit = fadeOut()
    ) {
        SplashScreen()
    }

    if (!showSplash) {
        if (loggedInUser == null) {
            AuthScreen(onAuthSuccess = { loggedInUser = it })
        } else {
            if (!showGreenWalk) {
                HomeScreen(
                    user = loggedInUser!!,
                    onLogout = {
                        viewModel.logout()
                        loggedInUser = null
                    },
                    onGreenWalkClick = { showGreenWalk = true }
                )
            } else {
                val db = UserDatabase.getDatabase(context)
                val repository = GreenWalkRepository(
                    dao = db.greenWalkDao(),
                    osrmApi = RetrofitClient.osrmApi,
                    overpassApi = RetrofitClient.overpassApi,
                    nominatimApi = RetrofitClient.nominatimApi
                )
                val greenWalkVm: GreenWalkViewModel = viewModel(
                    factory = GreenWalkViewModelFactory(repository)
                )
                GreenWalkScreen(
                    viewModel = greenWalkVm,
                    onBack = { showGreenWalk = false }
                )
            }
        }
    }
}
