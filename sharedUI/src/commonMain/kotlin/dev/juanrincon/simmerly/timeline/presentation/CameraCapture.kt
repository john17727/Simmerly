package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * Opens the device camera and hands back the captured photo, or returns null where there is no
 * camera to open (desktop), in which case the "Take photo" button is hidden.
 */
@Composable
expect fun rememberCameraCapture(onCaptured: (PlatformFile) -> Unit): (() -> Unit)?
