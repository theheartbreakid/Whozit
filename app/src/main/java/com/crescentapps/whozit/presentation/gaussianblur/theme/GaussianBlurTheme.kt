package com.crescentapps.whozit.presentation.gaussianblur.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.crescentapps.whozit.core.model.ThemeMode
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.presentation.gaussianblur.backdrop.LocalLiquidGlassEnabled
import com.crescentapps.whozit.presentation.theme.DarkWhozitColors
import com.crescentapps.whozit.presentation.theme.LightWhozitColors
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors

@Composable
fun GaussianBlurTheme(
    prefs: UserPreferences,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (prefs.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val gaussianColors = if (darkTheme) DarkGaussianBlurColors else LightGaussianBlurColors
    val whozitColors = if (darkTheme) DarkWhozitColors else LightWhozitColors
    val gaussianOptics = GaussianOptics.fromPreferences(prefs)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalLiquidGlassEnabled provides prefs.isLiquidGlassEnabled,
        LocalGaussianOptics provides gaussianOptics,
        LocalGaussianBlurColors provides gaussianColors,
        LocalGaussianBlurTokens provides GaussianBlurTokens,
        LocalWhozitColors provides whozitColors,
        com.crescentapps.whozit.presentation.components.LocalPrismalAdaptiveColor provides gaussianColors.textPrimary
    ) {
        content()
    }
}
