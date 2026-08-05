package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable

// Desktop has no equivalent screen-lock concept tied to a single view — no-op.
@Composable
actual fun KeepScreenOn() = Unit
