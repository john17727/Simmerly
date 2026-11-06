package dev.juanrincon.simmerly.recipes.presentation.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.Category
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.theme.SimmerlyTheme

internal enum class CategoryTagChipKind { Category, Tag, More }

/**
 * A category / tag / "+N" chip sized by [height]: 28dp on the hero image, 40dp in the Categories
 * and tags sheet. Tappable only when [onClick] is set; the hero group is tapped as a whole instead.
 */
@Composable
internal fun CategoryTagChip(
    label: String,
    kind: CategoryTagChipKind,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val container = when (kind) {
        CategoryTagChipKind.Category -> colors.primaryContainer
        CategoryTagChipKind.Tag -> colors.tertiaryContainer
        CategoryTagChipKind.More -> colors.surface
    }
    val content = when (kind) {
        CategoryTagChipKind.Category -> colors.onPrimaryContainer
        CategoryTagChipKind.Tag -> colors.onTertiaryContainer
        CategoryTagChipKind.More -> colors.onSurfaceVariant
    }
    val border = if (kind == CategoryTagChipKind.Tag) BorderStroke(1.dp, colors.tertiary) else null
    val body: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(height).padding(horizontal = if (height >= 40.dp) 14.dp else 10.dp)
        ) {
            Text(
                label,
                style = if (height >= 40.dp) MaterialTheme.typography.labelLarge
                else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            color = container,
            contentColor = content,
            border = border,
            modifier = modifier,
            content = body,
        )
    } else {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = container,
            contentColor = content,
            border = border,
            modifier = modifier,
            content = body,
        )
    }
}

/**
 * The chips over the phone hero image: the first category, the first tag and "+N" for the rest.
 * With no category the first two tags show instead. The whole group is one button ([onClick]),
 * which opens the full Categories and tags sheet. Renders nothing when both lists are empty.
 */
@Composable
internal fun HeroCategoryTagChips(
    categories: List<Category>,
    tags: List<Tag>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shownCategories = categories.take(1)
    val shownTags = tags.take(if (shownCategories.isEmpty()) 2 else 1)
    val hidden = categories.size + tags.size - shownCategories.size - shownTags.size
    if (shownCategories.isEmpty() && shownTags.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clickable(onClickLabel = "Show all categories and tags", role = Role.Button, onClick = onClick)
            // Read as one button instead of three unrelated labels.
            .clearAndSetSemantics {
                contentDescription = "Categories and tags: " +
                        (shownCategories.map { it.name } + shownTags.map { it.name }).joinToString() +
                        if (hidden > 0) ", and $hidden more" else ""
            }
    ) {
        shownCategories.forEach { CategoryTagChip(it.name, CategoryTagChipKind.Category, 28.dp) }
        shownTags.forEach { CategoryTagChip(it.name, CategoryTagChipKind.Tag, 28.dp) }
        if (hidden > 0) CategoryTagChip("+$hidden", CategoryTagChipKind.More, 28.dp)
    }
}

@Preview
@Composable
private fun HeroCategoryTagChipsPreview() {
    SimmerlyTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                HeroCategoryTagChips(
                    categories = listOf(Category("c1", "g", "Main Course"), Category("c2", "g", "Dinner")),
                    tags = listOf("Chicken", "Sheet Pan", "Weeknight").mapIndexed { i, n -> Tag("t$i", "g", n) },
                    onClick = {},
                )
            }
        }
    }
}
