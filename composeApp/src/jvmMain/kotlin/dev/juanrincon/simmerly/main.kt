package dev.juanrincon.simmerly

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.di.initKoin
import dev.juanrincon.simmerly.navigation.auth.DefaultRootComponent
import org.koin.java.KoinJavaComponent.get
import java.util.prefs.Preferences

fun main() {
    initKoin()
    val lifecycle = LifecycleRegistry()

    val authRepository: AuthRepository = get(AuthRepository::class.java)

    // Always create the root component outside Compose on the UI thread
    val root = runOnUiThread {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            storeFactory = DefaultStoreFactory(),
            authRepository = authRepository
        )
    }
    application {
        val prefs = remember { Preferences.userRoot().node("dev.juanrincon.simmerly.window") }
        val initialSize = remember {
            val w = prefs.getFloat("widthDp", 1200f)
            val h = prefs.getFloat("heightDp", 800f)
            DpSize(w.dp, h.dp)
        }

        val initialPlacement = remember {
            // Restore placement if it was maximized last time
            val p = prefs.get("placement", WindowPlacement.Floating.name)
            WindowPlacement.valueOf(p)
        }
        val windowState = rememberWindowState(size = initialSize, placement = initialPlacement)
        LifecycleController(lifecycle, windowState)
        // Persist when the window leaves composition (app close)
        DisposableEffect(Unit) {
            onDispose {
                // Only persist size when not maximized, otherwise keep last floating size
                if (windowState.placement == WindowPlacement.Floating) {
                    prefs.putFloat("widthDp", windowState.size.width.value)
                    prefs.putFloat("heightDp", windowState.size.height.value)
                }
                prefs.put("placement", windowState.placement.name)
            }
        }
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Simmerly",
        ) {
            App(root)
        }
    }
}