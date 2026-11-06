package dev.juanrincon.simmerly.recipes.presentation.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.Category
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/** Rows a chip group shows before collapsing the rest behind "+N more". */
private const val COLLAPSED_MAX_ROWS = 2

private val LABEL_COLUMN_WIDTH = 80.dp

/**
 * The expanded card's "Category" and "Tags" rows: a label column and a wrapping group of chips.
 * Each group shows at most two rows and folds the rest into a "+N more" chip, which expands it
 * to "Show less". Renders nothing when there are no categories and no tags.
 */
@Composable
fun CategoriesAndTags(
    categories: List<Category>,
    tags: List<Tag>,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty() && tags.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (categories.isNotEmpty()) {
            ChipGroupRow(label = if (categories.size == 1) "Category" else "Categories") {
                ChipGroup(
                    labels = categories.map { it.name },
                    chip = { CategoryChip(it) },
                )
            }
        }
        if (tags.isNotEmpty()) {
            ChipGroupRow(label = "Tags") {
                ChipGroup(
                    labels = tags.map { it.name },
                    chip = { TagChip(it) },
                )
            }
        }
    }
}

@Composable
private fun ChipGroupRow(label: String, content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(LABEL_COLUMN_WIDTH).padding(top = 8.dp)
        )
        Row(modifier = Modifier.weight(1f)) { content() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(
    labels: List<String>,
    chip: @Composable (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // ContextualFlowRow (unlike FlowRow) hands the overflow scope its item counts during
    // composition; reading them from a plain FlowRow's scope throws.
    ContextualFlowRow(
        itemCount = labels.size,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_MAX_ROWS,
        overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
            minRowsToShowCollapse = COLLAPSED_MAX_ROWS + 1,
            expandIndicator = {
                MoreChip(
                    label = "+${totalItemCount - shownItemCount} more",
                    onClick = { expanded = true },
                )
            },
            collapseIndicator = {
                TextButton(onClick = { expanded = false }) {
                    Text("Show less", style = MaterialTheme.typography.labelLarge)
                }
            },
        ),
        modifier = Modifier.fillMaxWidth()
    ) { index ->
        chip(labels[index])
    }
}

@Composable
private fun CategoryChip(label: String) {
    GroupChip(
        label = label,
        container = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        border = null,
    )
}

@Composable
private fun TagChip(label: String) {
    GroupChip(
        label = label,
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
    )
}

@Composable
private fun GroupChip(label: String, container: Color, content: Color, border: BorderStroke?) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
        border = border,
    ) {
        ChipLabel(label, FontWeight.Medium)
    }
}

@Composable
private fun ChipLabel(label: String, weight: FontWeight) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.height(32.dp).padding(horizontal = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = weight)
    }
}

@Composable
private fun MoreChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        ChipLabel(label, FontWeight.SemiBold)
    }
}

private fun sampleTags(vararg names: String) = names.mapIndexed { i, n -> Tag("t$i", "g", n) }
private fun sampleCategories(vararg names: String) = names.mapIndexed { i, n -> Category("c$i", "g", n) }

@Preview
@Composable
private fun CategoriesAndTagsPreview() {
    SimmerlyTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(16.dp)) {
                CategoriesAndTags(sampleCategories("Main Course"), sampleTags("Pasta"))
                CategoriesAndTags(
                    sampleCategories("Main Course", "Dinner", "Lunch"),
                    sampleTags(
                        "Pasta", "Italian", "Weeknight", "Quick", "Eggs", "Pork",
                        "Comfort food", "Date night", "High protein", "Kid friendly"
                    ),
                )
            }
        }
    }
}
