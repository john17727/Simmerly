package dev.juanrincon.simmerly

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    SimmerlyTheme {
        Text("Hello")
    }
}