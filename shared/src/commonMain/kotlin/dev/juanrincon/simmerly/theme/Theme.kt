package dev.juanrincon.simmerly.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable


@Composable
fun SimmerlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor) {
        rememberDynamicColorScheme(darkTheme)
            ?: if (darkTheme) SimmerlyDarkColorScheme else SimmerlyLightColorScheme
    } else {
        if (darkTheme) SimmerlyDarkColorScheme else SimmerlyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = simmerlyTypography(),
        content = content
    )
}
