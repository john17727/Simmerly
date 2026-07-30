package dev.juanrincon.simmerly.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun rememberDynamicColorScheme(darkTheme: Boolean): ColorScheme?
