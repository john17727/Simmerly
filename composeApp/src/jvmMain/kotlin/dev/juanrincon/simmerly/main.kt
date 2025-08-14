package dev.juanrincon.simmerly

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.juanrincon.simmerly.navigation.auth.DefaultRootComponent

fun main() {
    val lifecycle = LifecycleRegistry()

    // Always create the root component outside Compose on the UI thread
    val root = runOnUiThread {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            storeFactory = DefaultStoreFactory()
        )
    }
    application {
        val windowState = rememberWindowState(size = DpSize(1024.dp, 768.dp))
        LifecycleController(lifecycle, windowState)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Simmerly",
        ) {
            App(root)
        }
    }
}