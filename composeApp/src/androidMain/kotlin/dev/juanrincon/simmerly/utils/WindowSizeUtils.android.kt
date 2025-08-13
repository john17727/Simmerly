package dev.juanrincon.simmerly.utils

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@SuppressLint("ContextCastToActivity")
@Composable
actual fun calculatePlatformWindowSizeClass(): WindowSizeClass {
    val activity = LocalContext.current as? Activity
    // Provide a default fallback if no activity is found,
    // though in a typical Android app, an activity should be present.
    return if (activity != null) {
        calculateWindowSizeClass(activity)
    } else {
        // Fallback to a default (e.g., Compact width and height)
        // This situation should be rare in a running app.
        WindowSizeClass.calculateFromSize(androidx.compose.ui.unit.DpSize(0.dp, 0.dp))
    }
}