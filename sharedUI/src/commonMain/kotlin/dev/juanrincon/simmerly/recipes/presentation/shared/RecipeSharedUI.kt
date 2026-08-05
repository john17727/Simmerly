package dev.juanrincon.simmerly.recipes.presentation.shared

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
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi


@Composable
fun TagChip(name: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .border(1.dp, MaterialTheme.colorScheme.tertiary, MaterialTheme.shapes.small)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

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
            TagChip(name = tag.name)
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
        rating?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(it.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
        totalTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.Timer,
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
                    Icons.Rounded.LocalDining,
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
                    Icons.Rounded.LocalFireDepartment,
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        ingredient.formattedQuantity?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Text(
            text = ingredient.formattedDisplay,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
