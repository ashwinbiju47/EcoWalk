package com.example.EcoWalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.EcoWalk.ui.theme.BudgettrackerTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            var showMainScreen by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(2000)
                showMainScreen = true
            }

            if (showMainScreen) {
                BudgetTrackerApp()
            } else {
                SplashScreenUI()
            }
        }
    }
}

@Composable
fun SplashScreenUI() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Use the Image composable to display your logo
        Image(
            painter = painterResource(id = R.drawable.app_logo), // <-- Change to your image name
            contentDescription = "App Logo", // <-- Good practice for accessibility
            modifier = Modifier.size(128.dp) // <-- Adjust the size as needed
        )
    }
}

@Composable
fun BudgetTrackerApp() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Welcome to BudgetTracker!", style = MaterialTheme.typography.titleMedium)
        }
    }
}