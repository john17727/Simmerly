package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A persistent scrollbar for a scrollable pane. Draws nothing on Android, where a scroll position
 * is communicated by transient indicators and overscroll rather than a bar you can grab; on desktop
 * a pointer needs something to aim at, and a window with several independently scrolling panes
 * needs to show which ones have more content below.
 *
 * Place it in a [androidx.compose.foundation.layout.Box] over the scrolling content, aligned to the
 * end edge — it does not reserve layout space of its own.
 */
@Composable
expect fun VerticalScrollbar(scrollState: ScrollState, modifier: Modifier = Modifier)

/** [VerticalScrollbar] for a lazy list. */
@Composable
expect fun VerticalScrollbar(listState: LazyListState, modifier: Modifier = Modifier)
