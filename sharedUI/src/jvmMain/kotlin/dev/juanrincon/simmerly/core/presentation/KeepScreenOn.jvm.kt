package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Holds a `caffeinate -d` child process for as long as Cook Mode is composed, which is macOS's
 * supported way to keep the display from sleeping without touching power settings. It dies with
 * the process, so a crash can't leave the display pinned awake.
 *
 * macOS only. Windows would need `SetThreadExecutionState` and Linux `systemd-inhibit`, both of
 * which mean a new dependency, so on those platforms this stays the no-op it has always been and
 * Cook Mode's "screen stays awake" copy overstates what the app is doing.
 */
@Composable
actual fun KeepScreenOn() {
    DisposableEffect(Unit) {
        val caffeinate = if (isMac) {
            // A missing or sandboxed binary just means we degrade to the old no-op.
            runCatching { ProcessBuilder("caffeinate", "-d").start() }.getOrNull()
        } else {
            null
        }
        onDispose { caffeinate?.destroy() }
    }
}

private val isMac: Boolean
    get() = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)
