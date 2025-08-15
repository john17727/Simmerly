package dev.juanrincon.simmerly.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable

@Composable
expect fun calculatePlatformWindowSizeClass(): WindowSizeClass