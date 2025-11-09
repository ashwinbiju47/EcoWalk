package com.example.ecowalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ecowalk.ui.AppRoot
import com.example.ecowalk.ui.theme.EcoWalkTheme   // ✅ import your custom theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoWalkTheme(    // ✅ apply your green eco theme here
                darkTheme = false,   // or true / isSystemInDarkTheme()
                dynamicColor = false // disable wallpaper colors
            ) {
                AppRoot()
            }
        }
    }
}
