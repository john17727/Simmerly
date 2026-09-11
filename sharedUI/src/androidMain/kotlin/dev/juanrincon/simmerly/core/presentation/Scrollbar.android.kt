package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Android surfaces scroll position through overscroll and transient indicators; a persistent
// grabbable bar would be foreign there — no-op.
@Composable
actual fun VerticalScrollbar(scrollState: ScrollState, modifier: Modifier) = Unit

@Composable
actual fun VerticalScrollbar(listState: LazyListState, modifier: Modifier) = Unit
