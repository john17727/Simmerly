package dev.juanrincon.simmerly

import androidx.compose.ui.window.ComposeUIViewController
import dev.juanrincon.simmerly.decompose.RootComponent

fun MainViewController(root: RootComponent) = ComposeUIViewController { App(root) }