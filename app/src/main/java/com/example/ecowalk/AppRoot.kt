package com.example.ecowalk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.example.ecowalk.ui.navigation.BottomNavItem
import com.example.ecowalk.ui.profile.ProfileScreen
import com.example.ecowalk.ui.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.example.ecowalk.utils.LocationClient

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()

    val auth = FirebaseAuth.getInstance()
    var loggedInUser by remember { mutableStateOf<User?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    // Initialize GreenWalk ViewModel
    // Initialize GreenWalk ViewModel

    val db = UserDatabase.getDatabase(context)
    val locationClient = LocationClient(context)
    val repository = GreenWalkRepository(
        dao = db.greenWalkDao(),
        osrmApi = RetrofitClient.osrmApi,
        overpassApi = RetrofitClient.overpassApi,
        nominatimApi = RetrofitClient.nominatimApi
    )
    val greenWalkVm: GreenWalkViewModel = viewModel(
        factory = GreenWalkViewModelFactory(repository, locationClient)
    )

    LaunchedEffect(Unit) {
        delay(1500) // Slightly longer splash for effect
        showSplash = false

        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val localUser = authViewModel.getLocalUser(firebaseUser.uid)
            loggedInUser = localUser
        }
    }

    if (showSplash) {
        SplashScreen()
    } else {
        if (loggedInUser == null) {
            AuthScreen(onAuthSuccess = { loggedInUser = it })
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        val items = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.GreenWalk,
                            BottomNavItem.Profile
                        )

                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomNavItem.Home.route) {
                        val history by greenWalkVm.walkHistory.collectAsState()
                        HomeScreen(
                            user = loggedInUser!!,
                            walkHistory = history
                        )
                    }
                    composable(BottomNavItem.GreenWalk.route) {
                        GreenWalkScreen(
                            viewModel = greenWalkVm,
                            onBack = { navController.navigate(BottomNavItem.Home.route) } // Optional: handle back in GreenWalk
                        )
                    }
                    composable(BottomNavItem.Profile.route) {
                        ProfileScreen(
                            user = loggedInUser!!,
                            onLogout = {
                                authViewModel.logout()
                                loggedInUser = null
                            }
                        )
                    }
                }
            }
        }
    }
}
