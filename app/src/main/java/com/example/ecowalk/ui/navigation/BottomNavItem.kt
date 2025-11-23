package com.example.ecowalk.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object GreenWalk : BottomNavItem("green_walk", Icons.Default.Terrain, "Green Walk")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
