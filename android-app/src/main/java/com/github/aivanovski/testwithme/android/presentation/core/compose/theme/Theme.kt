package com.github.aivanovski.testwithme.android.presentation.core.compose.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Immutable
data class Theme(
    val colors: AppColors,
    val materialColors: ColorScheme,
)

val LightTheme = Theme(
    colors = LightAppColors,
    materialColors = lightColorScheme(
        primary = LightAppColors.primary,
        secondary = LightAppColors.secondary,
        tertiary = LightAppColors.tertiary
    )
)

val DarkTheme = Theme(
    colors = DarkAppColors,
    materialColors = darkColorScheme(
        primary = DarkAppColors.primary,
        secondary = DarkAppColors.secondary,
        tertiary = DarkAppColors.tertiary
    )
)

val LocalExtendedColors = staticCompositionLocalOf {
    LightTheme
}

@Composable
fun AppTheme(
    theme: Theme,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = theme.materialColors.primary.toArgb()
            // WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides theme) {
        MaterialTheme(
            colorScheme = theme.materialColors,
            content = content
        )
    }
}

object AppTheme {
    val theme: Theme
        @Composable
        get() = LocalExtendedColors.current
}