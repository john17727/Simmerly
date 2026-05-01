package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.recipe_1
import simmerly.composeapp.generated.resources.recipe_10
import simmerly.composeapp.generated.resources.recipe_11
import simmerly.composeapp.generated.resources.recipe_12
import simmerly.composeapp.generated.resources.recipe_13
import simmerly.composeapp.generated.resources.recipe_14
import simmerly.composeapp.generated.resources.recipe_2
import simmerly.composeapp.generated.resources.recipe_3
import simmerly.composeapp.generated.resources.recipe_4
import simmerly.composeapp.generated.resources.recipe_5
import simmerly.composeapp.generated.resources.recipe_6
import simmerly.composeapp.generated.resources.recipe_7
import simmerly.composeapp.generated.resources.recipe_8
import simmerly.composeapp.generated.resources.recipe_9
import simmerly.composeapp.generated.resources.simmerly_logo

private const val COLUMNS = 5
private val GAP = 8.dp

@Composable
internal fun WelcomeCollage(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val overflow = maxWidth / 8
        val rowWidth = maxWidth + overflow * 2
        val cellSize = (rowWidth - GAP * (COLUMNS - 1)) / COLUMNS

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
            horizontalArrangement = Arrangement.spacedBy(GAP)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                ColorBlock(MaterialTheme.colorScheme.tertiary, cellSize)
                RecipeSquareImage(Res.drawable.recipe_1, cellSize)
                RecipeSquareImage(Res.drawable.recipe_2, cellSize)
                ColorBlock(MaterialTheme.colorScheme.secondary, cellSize)
            }
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                RecipeSquareImage(Res.drawable.recipe_3, cellSize)
                LogoCell(cellSize)
                RecipeSquareImage(Res.drawable.recipe_4, cellSize)
                RecipeCircleImage(Res.drawable.recipe_5, cellSize)
            }
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                RecipeCircleImage(Res.drawable.recipe_6, cellSize)
                RecipeSquareImage(Res.drawable.recipe_7, cellSize)
                ColorBlock(MaterialTheme.colorScheme.primary, cellSize)
                RecipeSquareImage(Res.drawable.recipe_8, cellSize)
            }
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                ColorBlock(MaterialTheme.colorScheme.secondaryContainer, cellSize)
                RecipeSquareImage(Res.drawable.recipe_9, cellSize)
                RecipeCircleImage(Res.drawable.recipe_10, cellSize)
                RecipeSquareImage(Res.drawable.recipe_11, cellSize)
            }
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                RecipeSquareImage(Res.drawable.recipe_12, cellSize)
                RecipeSquareImage(Res.drawable.recipe_13, cellSize)
                RecipeSquareImage(Res.drawable.recipe_14, cellSize)
                ColorBlock(MaterialTheme.colorScheme.tertiaryContainer, cellSize)
            }
        }
    }
}

@Composable
private fun RecipeSquareImage(resource: DrawableResource, size: Dp) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(MaterialTheme.shapes.medium)
    )
}

@Composable
private fun RecipeCircleImage(resource: DrawableResource, size: Dp) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape)
    )
}

@Composable
private fun ColorBlock(color: Color, size: Dp) {
    Box(
        modifier = Modifier.size(size).background(color, MaterialTheme.shapes.medium)
    )
}

@Composable
private fun LogoCell(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.simmerly_logo),
            contentDescription = "Simmerly",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.sizeIn(maxWidth = size * 0.6f, maxHeight = size * 0.6f)
        )
    }
}
