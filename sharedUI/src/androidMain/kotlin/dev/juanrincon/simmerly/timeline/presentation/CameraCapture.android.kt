package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberCameraCapture(onCaptured: (PlatformFile) -> Unit): (() -> Unit)? {
    val launcher = rememberCameraPickerLauncher { file -> if (file != null) onCaptured(file) }
    return remember(launcher) { { launcher.launch() } }
}
