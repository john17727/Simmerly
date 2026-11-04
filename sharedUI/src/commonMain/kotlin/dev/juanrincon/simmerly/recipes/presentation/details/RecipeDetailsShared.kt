package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.core.presentation.shimmerColors

/**
 * A recipe section (ingredients, instructions, notes, nutrition) in its card. While
 * [loadingHeight] is set the card is a shimmering placeholder of that height instead.
 */
@Composable
internal fun RecipeSectionCard(
    colors: CardColors,
    modifier: Modifier = Modifier,
    loadingHeight: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        colors = colors,
        modifier = modifier.ifTrue(loadingHeight != null) {
            height(loadingHeight!!).shimmer(
                colors = shimmerColors(),
                shape = MaterialTheme.shapes.medium
            )
        },
        content = content
    )
}

/**
 * The extended "Start cooking" button shared by the foldable and desktop layouts. No steps to cook
 * means nothing for Cook Mode to show, so it stays hidden rather than opening an empty stepper.
 */
@Composable
internal fun StartCookingFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            text = { Text("Start cooking") },
        )
    }
}
