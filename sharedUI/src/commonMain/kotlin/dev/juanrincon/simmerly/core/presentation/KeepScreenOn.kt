package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable

/**
 * Keeps the display awake for as long as the calling composable is in composition. Cook Mode
 * calls this once at its root — a cook's hands are usually busy, so the screen shouldn't dim or
 * lock mid-recipe.
 */
@Composable
expect fun KeepScreenOn()
