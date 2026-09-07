package com.fasaldrishti.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = ObsidianVoid,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldPrimary,
    secondary = SolarGold,
    onSecondary = ObsidianVoid,
    secondaryContainer = SolarContainerDark,
    onSecondaryContainer = SolarGoldVariant,
    tertiary = NeonLime,
    background = ObsidianVoid,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianCardBorder,
    error = CrimsonCoral,
    errorContainer = CrimsonContainerDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldDark,
    secondary = SolarGold,
    onSecondary = Color.White,
    secondaryContainer = SolarContainerLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = NeonLime,
    background = FrostBackground,
    onBackground = TextPrimaryLight,
    surface = FrostSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = FrostElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = FrostCardBorder,
    error = CrimsonCoral,
    errorContainer = CrimsonContainerLight,
    onError = Color.White
)

@Composable
fun FasalDrishtiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
