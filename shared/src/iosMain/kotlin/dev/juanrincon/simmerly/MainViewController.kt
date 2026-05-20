package dev.juanrincon.simmerly

import androidx.compose.ui.window.ComposeUIViewController
import dev.juanrincon.simmerly.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) { App() }