package dev.juanrincon.simmerly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.decompose.RootComponent
import dev.juanrincon.simmerly.decompose.RootContent
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import dev.juanrincon.simmerly.utils.calculatePlatformWindowSizeClass
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(rootComponent: RootComponent) {
    val windowSizeClass = calculatePlatformWindowSizeClass()
    SimmerlyTheme {
        RootContent(rootComponent, modifier = Modifier.fillMaxSize(), windowSizeClass)
    }
}