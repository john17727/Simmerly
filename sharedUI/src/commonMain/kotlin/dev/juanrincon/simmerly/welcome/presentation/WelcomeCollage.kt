package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.theme.Simmerly
import org.jetbrains.compose.resources.painterResource
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.simmerly_logo

private const val COMPACT_COLUMNS = 5
private val COMPACT_GAP = 8.dp

private const val EXPANDED_COLUMNS = 4
private val EXPANDED_GAP = 10.dp

/**
 * Phone layout: five columns over three rows, bled past both edges so the grid
 * reads as a band rather than a boxed-in tile. Denser and shorter than the
 * desktop grid, which matters because the whole form sits below it.
 */
@Composable
internal fun WelcomeCollageCompact(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val overflow = maxWidth / 8
        val rowWidth = maxWidth + overflow * 2
        val cellSize = (rowWidth - COMPACT_GAP * (COMPACT_COLUMNS - 1)) / COMPACT_COLUMNS

        Row(
            modifier = Modifier.layout { measurable, constraints ->
                val overflowPx = overflow.roundToPx()
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.maxWidth + overflowPx * 2,
                        maxWidth = constraints.maxWidth + overflowPx * 2
                    )
                )
                layout(constraints.maxWidth, placeable.height) {
                    placeable.place(-overflowPx, 0)
                }
            },
            horizontalArrangement = Arrangement.spacedBy(COMPACT_GAP)
        ) {
            CompactColumn {
                RecipeSquareImage("drawable/recipe_1.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_2.jpg", cellSize)
                ColorBlock(MaterialTheme.colorScheme.tertiary, cellSize)
            }
            CompactColumn {
                ColorBlock(MaterialTheme.colorScheme.secondaryContainer, cellSize)
                LogoCell(cellSize)
                RecipeCircleImage("drawable/recipe_3.jpg", cellSize)
            }
            CompactColumn {
                RecipeSquareImage("drawable/recipe_5.jpg", cellSize)
                ColorBlock(MaterialTheme.colorScheme.primary, cellSize)
                RecipeSquareImage("drawable/recipe_6.jpg", cellSize)
            }
            CompactColumn {
                RecipeSquareImage("drawable/recipe_7.jpg", cellSize)
                RecipeCircleImage("drawable/recipe_8.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_9.jpg", cellSize)
            }
            CompactColumn {
                RecipeSquareImage("drawable/recipe_10.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_11.jpg", cellSize)
                ColorBlock(MaterialTheme.colorScheme.tertiaryContainer, cellSize)
            }
        }
    }
}

/**
 * Desktop layout: the design's 4x4 grid, contained rather than bled, sized to
 * fill its panel beside the form.
 */
@Composable
internal fun WelcomeCollageExpanded(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val cellSize =
            (maxWidth - EXPANDED_GAP * (EXPANDED_COLUMNS - 1)) / EXPANDED_COLUMNS

        Column(verticalArrangement = Arrangement.spacedBy(EXPANDED_GAP)) {
            ExpandedRow {
                RecipeSquareImage("drawable/recipe_1.jpg", cellSize)
                ColorBlock(Simmerly.Coral100, cellSize)
                RecipeSquareImage("drawable/recipe_5.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_7.jpg", cellSize)
            }
            ExpandedRow {
                RecipeSquareImage("drawable/recipe_2.jpg", cellSize)
                LogoCell(cellSize)
                ColorBlock(MaterialTheme.colorScheme.primary, cellSize)
                RecipeCircleImage("drawable/recipe_8.jpg", cellSize)
            }
            ExpandedRow {
                ColorBlock(MaterialTheme.colorScheme.tertiary, cellSize)
                RecipeCircleImage("drawable/recipe_9.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_6.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_10.jpg", cellSize)
            }
            ExpandedRow {
                RecipeSquareImage("drawable/recipe_11.jpg", cellSize)
                RecipeSquareImage("drawable/recipe_12.jpg", cellSize)
                RecipeCircleImage("drawable/recipe_4.jpg", cellSize)
                ColorBlock(Simmerly.HerbSageSubtle, cellSize)
            }
        }
    }
}

@Composable
private fun CompactColumn(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(COMPACT_GAP)) { content() }
}

@Composable
private fun ExpandedRow(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(EXPANDED_GAP), content = content)
}

@Composable
private fun RecipeSquareImage(path: String, size: Dp) {
    AsyncImage(
        model = Res.getUri(path), // Need to use Res.getUri(path) see: https://github.com/coil-kt/coil/issues/2812
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(MaterialTheme.shapes.medium)
    )
}

@Composable
private fun RecipeCircleImage(path: String, size: Dp) {
    AsyncImage(
        model = Res.getUri(path), // Need to use Res.getUri(path) see: https://github.com/coil-kt/coil/issues/2812
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape)
    )
}

@Composable
private fun ColorBlock(color: Color, size: Dp) {
    Box(modifier = Modifier.size(size).background(color, MaterialTheme.shapes.medium))
}

@Composable
private fun LogoCell(size: Dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Res.drawable.simmerly_logo),
            contentDescription = "Simmerly",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.sizeIn(maxWidth = size * 0.56f, maxHeight = size * 0.56f)
        )
    }
}
