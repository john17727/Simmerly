package dev.juanrincon.simmerly.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun calculatePlatformWindowSizeClass(): WindowSizeClass {
    val windowPixelSize: IntSize = LocalWindowInfo.current.containerSize // This is IntSize
    val density = LocalDensity.current // Get the current density

    // Convert IntSize (pixels) to DpSize
    val windowDpSize = with(density) {
        DpSize(
            width = windowPixelSize.width.toDp(),
            height = windowPixelSize.height.toDp()
        )
    }

    return WindowSizeClass.calculateFromSize(windowDpSize)
}