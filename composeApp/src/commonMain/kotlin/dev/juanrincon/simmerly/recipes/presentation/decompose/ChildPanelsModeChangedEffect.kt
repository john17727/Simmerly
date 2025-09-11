@file:OptIn(ExperimentalDecomposeApi::class)

package dev.juanrincon.simmerly.recipes.presentation.decompose

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode

@Composable
fun ChildPanelsModeChangedEffect(onModeChanged: (ChildPanelsMode) -> Unit) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val mode =
        if (windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) ChildPanelsMode.DUAL else ChildPanelsMode.SINGLE

    DisposableEffect(mode) {
        onModeChanged(mode)
        onDispose {}
    }
}