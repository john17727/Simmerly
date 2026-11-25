package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberCameraCapture(onCaptured: (PlatformFile) -> Unit): (() -> Unit)? = null
