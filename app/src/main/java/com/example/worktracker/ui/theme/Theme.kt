package com.example.worktracker.ui.theme

import android.app.Activity
// import android.os.Build // No longer strictly needed
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
// import androidx.compose.material3.dynamicDarkColorScheme // No longer needed
// import androidx.compose.material3.dynamicLightColorScheme // No longer needed
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
// import androidx.compose.ui.graphics.Color // No longer needed for this
// import androidx.compose.ui.graphics.toArgb // No longer needed for this
// import androidx.compose.ui.platform.LocalContext // No longer needed
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WorkTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Changed default to false
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) { // Simplified logic
        darkColorScheme(
            primary = Dark_Primary,
            onPrimary = Dark_OnPrimary,
            primaryContainer = Dark_PrimaryContainer,
            onPrimaryContainer = Dark_OnPrimaryContainer,
            secondary = Dark_Secondary,
            onSecondary = Dark_OnSecondary,
            secondaryContainer = Dark_SecondaryContainer,
            onSecondaryContainer = Dark_OnSecondaryContainer,
            tertiary = Dark_Tertiary,
            onTertiary = Dark_OnTertiary,
            tertiaryContainer = Dark_TertiaryContainer,
            onTertiaryContainer = Dark_OnTertiaryContainer,
            background = Dark_Background,
            onBackground = Dark_OnBackground,
            surface = Dark_Surface,
            onSurface = Dark_OnSurface,
            error = Dark_Error,
            onError = Dark_OnError
        )
    } else {
        lightColorScheme(
            primary = Light_Primary,
            onPrimary = Light_OnPrimary,
            primaryContainer = Light_PrimaryContainer,
            onPrimaryContainer = Light_OnPrimaryContainer,
            secondary = Light_Secondary,
            onSecondary = Light_OnSecondary,
            secondaryContainer = Light_SecondaryContainer,
            onSecondaryContainer = Light_OnSecondaryContainer,
            tertiary = Light_Tertiary,
            onTertiary = Light_OnTertiary,
            tertiaryContainer = Light_TertiaryContainer,
            onTertiaryContainer = Light_OnTertiaryContainer,
            background = Light_Background,
            onBackground = Light_OnBackground,
            surface = Light_Surface,
            onSurface = Light_OnSurface,
            error = Light_Error,
            onError = Light_OnError
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // With enableEdgeToEdge, transparency is handled by the system.
            // We only need to set the light/dark appearance of system bar icons.
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme // Ensure nav bar icons also follow theme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
