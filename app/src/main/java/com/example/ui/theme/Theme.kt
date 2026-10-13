package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraCyan,
    onPrimary = AuraDarkBackground,
    primaryContainer = AuraSurfaceElevated,
    onPrimaryContainer = AuraCyan,
    secondary = AuraElectricBlue,
    onSecondary = AuraDarkBackground,
    secondaryContainer = AuraSurfaceCard,
    onSecondaryContainer = AuraTextPrimary,
    tertiary = AuraViolet,
    onTertiary = AuraDarkBackground,
    background = AuraDarkBackground,
    onBackground = AuraTextPrimary,
    surface = AuraSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraSurfaceElevated,
    onSurfaceVariant = AuraTextSecondary,
    error = AuraError,
    onError = AuraDarkBackground
)

@Composable
fun AuraTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AuraDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AuraDarkBackground.toArgb()
            window.navigationBarColor = AuraDarkBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        content = content
    )
}
