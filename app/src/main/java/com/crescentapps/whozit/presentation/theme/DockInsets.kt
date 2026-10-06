package com.crescentapps.whozit.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class DockSafeAreaState {
    var dockHeight by mutableStateOf(0.dp)
}

/**
 * Shared CompositionLocal to provide a consistent dock-aware inset strategy
 * across all three Whozit UI renderers (Gaussian, Liquid, M3Expressive).
 */
val LocalDockSafeArea = compositionLocalOf { DockSafeAreaState() }

@Composable
fun PaddingValues.withDockPadding(extraBottom: Dp = 16.dp): PaddingValues {
    val dock = LocalDockSafeArea.current.dockHeight
    val layoutDirection = LocalLayoutDirection.current
    
    val additional = if (dock > 0.dp) dock + extraBottom else 0.dp
    
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding(),
        end = this.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + additional
    )
}

/**
 * Creates WindowInsets for a Scaffold that replaces the bottom system bar inset
 * with the dock height (which already includes the system nav bar) plus semantic spacing.
 */
@Composable
fun whozitScaffoldWindowInsets(extraBottom: Dp = 16.dp): WindowInsets {
    val dock = LocalDockSafeArea.current.dockHeight
    val systemBars = WindowInsets.systemBars
    
    return if (dock > 0.dp) {
        systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .add(WindowInsets(bottom = dock + extraBottom))
    } else {
        systemBars
    }
}
