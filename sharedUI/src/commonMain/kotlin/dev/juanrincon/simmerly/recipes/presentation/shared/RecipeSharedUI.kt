package dev.juanrincon.simmerly.recipes.presentation.shared

import dev.juanrincon.simmerly.core.presentation.SimmerlyChip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagRow(tags: List<Tag>, modifier: Modifier = Modifier) {
    if (tags.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        tags.forEach { tag ->
            SimmerlyChip(label = tag.name)
        }
    }
}

@Composable
fun RecipeMetaRow(
    rating: Double?,
    totalTime: String?,
    prepTime: String?,
    cookTime: String?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        RatingStars(rating)
        totalTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        prepTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        cookTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** A row of [IngredientChip]s, used both by the recipe detail's instruction list and by Cook
 * Mode's per-step ingredient callouts. Renders nothing for an empty list. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientChipRow(ingredients: List<IngredientUi>, modifier: Modifier = Modifier) {
    if (ingredients.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        ingredients.forEach { ingredient ->
            IngredientChip(ingredient)
        }
    }
}

@Composable
fun IngredientChip(ingredient: IngredientUi, modifier: Modifier = Modifier) {
    SimmerlyChip(
        label = ingredient.formattedDisplay,
        emphasis = ingredient.formattedQuantity,
        bordered = false,
        modifier = modifier,
    )
}

private const val MAX_STARS = 5

/**
 * The user's own rating as five tappable stars with the value and a hint. Tapping the star that
 * matches the current rating clears it. [onRatingChange] gets a whole number of stars, or `null`.
 */
@Composable
fun RatingInput(
    rating: Double?,
    onRatingChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filled = rating?.roundToInt()?.coerceIn(0, MAX_STARS) ?: 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Row {
            for (star in 1..MAX_STARS) {
                val on = star <= filled
                IconButton(
                    onClick = { onRatingChange(if (star == filled) null else star.toDouble()) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (on) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (star == 1) "Rate 1 star" else "Rate $star stars",
                        tint = if (on) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
        if (filled > 0) {
            Text(
                filled.toDouble().toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            if (filled > 0) "Your rating" else "Not rated yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Five stars filled up to [rating] (rounded to the nearest whole star), then the value. An unrated
 * recipe shows five empty stars and no number.
 */
@Composable
fun RatingStars(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    val filled = rating?.let { it.roundToInt().coerceIn(0, MAX_STARS) } ?: 0
    val description = if (rating == null) "Not rated" else "Rated $rating out of $MAX_STARS"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        repeat(MAX_STARS) { index ->
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = if (index < filled) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize)
            )
        }
        rating?.let {
            Text(
                it.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/**
 * Total, prep and cook time as a bordered block. Total is tinted; prep and cook are hidden when
 * unset. With only a total it collapses to a single full-width row.
 */
@Composable
fun RecipeTimes(
    totalTime: String?,
    prepTime: String?,
    cookTime: String?,
    modifier: Modifier = Modifier
) {
    val cells = listOfNotNull(
        totalTime?.let { TimeCell("Total", Icons.Outlined.Timer, it, highlighted = true) },
        prepTime?.let { TimeCell("Prep", Icons.Outlined.Restaurant, it) },
        cookTime?.let { TimeCell("Cook", Icons.Outlined.LocalFireDepartment, it) },
    )
    if (cells.isEmpty()) return
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    ) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) {
                Box(
                    Modifier.fillMaxHeight().width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            TimeCellContent(
                cell,
                inline = cells.size == 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class TimeCell(
    val label: String,
    val icon: ImageVector,
    val value: String,
    val highlighted: Boolean = false,
)

@Composable
private fun TimeCellContent(cell: TimeCell, inline: Boolean, modifier: Modifier = Modifier) {
    val labelColor = if (cell.highlighted) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val background = if (cell.highlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    else Color.Transparent
    val label: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(cell.icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(16.dp))
            Text(cell.label, style = MaterialTheme.typography.labelMedium, color = labelColor)
        }
    }
    val value: @Composable () -> Unit = {
        Text(
            cell.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
    if (inline) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.background(background).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            label()
            value()
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier.background(background).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            label()
            value()
        }
    }
}

@Preview
@Composable
private fun RatingStarsPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        RatingStars(5.0)
        RatingStars(3.4)
        RatingStars(null)
    }
}

@Preview
@Composable
private fun RecipeTimesPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        RecipeTimes("30 min", "10 min", "20 min")
        RecipeTimes("3 hr 20 min", "20 min", "3 hr")
        RecipeTimes("45 min", null, null)
    }
}
