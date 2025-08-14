package dev.juanrincon.simmerly

import androidx.compose.ui.window.ComposeUIViewController
import dev.juanrincon.simmerly.navigation.auth.RootComponent

fun MainViewController(root: RootComponent) = ComposeUIViewController { App(root) }