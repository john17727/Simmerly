package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's standard container: a medium-rounded surface with a 1dp `outlineVariant` border.
 * Pass a tinted [color] (`surfaceContainerLow`) for cards that sit on the page background and
 * [shadowElevation] for ones that float above content, like suggestion dropdowns.
 */
@Composable
fun OutlinedSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = shadowElevation,
        modifier = modifier,
        content = content,
    )
}
