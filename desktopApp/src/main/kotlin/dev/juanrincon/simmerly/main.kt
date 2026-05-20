package dev.juanrincon.simmerly

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.juanrincon.simmerly.di.initKoin
import java.util.prefs.Preferences

fun main() {
    initKoin()
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
            App()
        }
    }
}