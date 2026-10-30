package dev.juanrincon.simmerly.core.presentation.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * Window width from which the app lays screens out as side-by-side panes and uses the desktop
 * designs. This is deliberately above the Expanded size class (840dp): an unfolded foldable sits
 * just over 840dp, and once the navigation rail is taken off there isn't room for a list next to
 * a detail pane. Below this the app shows one pane at a time.
 */
const val TWO_PANE_MIN_WIDTH_DP = 1000

/** Whether the window is wide enough for the two-pane / desktop layouts. */
@Composable
fun isTwoPaneWidth(): Boolean = currentWindowAdaptiveInfoV2().windowSizeClass
    .isWidthAtLeastBreakpoint(TWO_PANE_MIN_WIDTH_DP)

/** Whether the window is at least Medium width (600dp), where the phone's single column stretches. */
@Composable
fun isMediumWidthOrWider(): Boolean = currentWindowAdaptiveInfoV2().windowSizeClass
    .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

/**
 * The default [PaneScaffoldDirective], held to a single pane until [TWO_PANE_MIN_WIDTH_DP] so a
 * list-detail scene and the screens inside it always switch layout at the same width.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberTwoPaneDirective(): PaneScaffoldDirective {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
    return if (isTwoPaneWidth()) {
        directive
    } else {
        directive.copy(maxHorizontalPartitions = 1, horizontalPartitionSpacerSize = 0.dp)
    }
}
