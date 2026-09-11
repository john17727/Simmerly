package dev.juanrincon.simmerly.core.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import androidx.compose.foundation.VerticalScrollbar as FoundationVerticalScrollbar

/** How long the bar lingers after the last scroll before fading out. */
private const val LINGER_MILLIS = 900L
private const val FADE_IN_MILLIS = 100
private const val FADE_OUT_MILLIS = 400

@Composable
actual fun VerticalScrollbar(scrollState: ScrollState, modifier: Modifier) {
    AutoHidingScrollbar(
        positionKey = { scrollState.value },
        isDragging = { scrollState.isScrollInProgress },
        modifier = modifier
    ) { barModifier ->
        FoundationVerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            style = simmerlyScrollbarStyle(),
            modifier = barModifier
        )
    }
}

@Composable
actual fun VerticalScrollbar(listState: LazyListState, modifier: Modifier) {
    AutoHidingScrollbar(
        positionKey = { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset },
        isDragging = { listState.isScrollInProgress },
        modifier = modifier
    ) { barModifier ->
        FoundationVerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            style = simmerlyScrollbarStyle(),
            modifier = barModifier
        )
    }
}

/**
 * Shows the bar while the pane is being scrolled and fades it out once it settles, the way an
 * overlay scrollbar behaves — the panes are content, and a permanent rule down every one of them
 * competes with it.
 *
 * Keyed off the scroll *position* rather than `isScrollInProgress` alone, because a mouse wheel
 * moves the offset in discrete jumps that can settle between frames without ever reporting a
 * scroll as in progress. Hovering pins it open so there is still something to grab: the bar stays
 * laid out and hit-testable at zero alpha, so the pointer finds it at the pane's edge.
 */
@Composable
private fun AutoHidingScrollbar(
    positionKey: () -> Any?,
    isDragging: () -> Boolean,
    modifier: Modifier,
    bar: @Composable (Modifier) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    var recentlyScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow(positionKey)
            // The first emission is the initial position, not a scroll — otherwise every pane
            // would flash a scrollbar on arrival.
            .drop(1)
            .collectLatest {
                recentlyScrolled = true
                delay(LINGER_MILLIS)
                recentlyScrolled = false
            }
    }

    val visible = hovered || recentlyScrolled || isDragging()
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) FADE_IN_MILLIS else FADE_OUT_MILLIS),
        label = "ScrollbarAlpha"
    )

    bar(modifier.hoverable(interactionSource).alpha(alpha))
}

/**
 * The design's pane scrollbar: a fully rounded thumb in the border grey, darkening on hover. The
 * default style is a hardcoded black at 12%/50% alpha, which disappears against a dark theme —
 * these resolve through the color scheme instead.
 */
@Composable
private fun simmerlyScrollbarStyle(): ScrollbarStyle {
    val unhover = MaterialTheme.colorScheme.outlineVariant
    val hover = MaterialTheme.colorScheme.outline
    return remember(unhover, hover) {
        ScrollbarStyle(
            minimalHeight = 24.dp,
            thickness = 8.dp,
            shape = RoundedCornerShape(percent = 50),
            hoverDurationMillis = 300,
            unhoverColor = unhover,
            hoverColor = hover
        )
    }
}
