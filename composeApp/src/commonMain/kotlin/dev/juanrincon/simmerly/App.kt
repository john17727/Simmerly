package dev.juanrincon.simmerly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.navigation.auth.RootComponent
import dev.juanrincon.simmerly.navigation.auth.RootContent
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(rootComponent: RootComponent) {
    SimmerlyTheme {
        RootContent(rootComponent, modifier = Modifier.fillMaxSize())
    }
}