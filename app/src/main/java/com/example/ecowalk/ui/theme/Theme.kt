package com.example.ecowalk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val LightColorScheme = lightColorScheme(
    primary = EcoGreenPrimary,
    secondary = EcoGreenSecondary,
    tertiary = EcoBrown,
    background = EcoBackground,
    surface = EcoSurface,
    onPrimary = EcoOnPrimary,
    onSecondary = Color.White,
    onBackground = EcoOnBackground,
    onSurface = EcoOnBackground
)

private val DarkColorScheme = darkColorScheme(
    primary = EcoGreenPrimaryDark,
    secondary = EcoGreenSecondaryDark,
    tertiary = EcoBrown,
    background = EcoBackgroundDark,
    surface = EcoSurfaceDark,
    onPrimary = EcoOnPrimaryDark,
    onSecondary = EcoOnPrimaryDark,
    onBackground = EcoOnBackgroundDark,
    onSurface = EcoOnBackgroundDark
)

@Composable
fun EcoWalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
