package dev.juanrincon.simmerly.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun calculatePlatformWindowSizeClass(): WindowSizeClass {
    val uiViewController = LocalUIViewController.current
    val viewDpSize = remember(uiViewController) {
        val bounds = uiViewController.view.bounds
        val widthInPoints = bounds.useContents { this.size.width }
        val heightInPoints = bounds.useContents { this.size.height }
        // Correctly construct DpSize using the Dp values
        DpSize(width = widthInPoints.dp, height = heightInPoints.dp)
    }

    return WindowSizeClass.calculateFromSize(viewDpSize)
}