package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.Category
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.recipes.presentation.shared.CategoryTagChip
import dev.juanrincon.simmerly.recipes.presentation.shared.CategoryTagChipKind
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import kotlinx.coroutines.launch

/**
 * The phone "Categories and tags" sheet: every category and tag of the recipe.
 *
 * Tapping a chip is meant to list every recipe that has it, but the Recipes list can't filter by
 * category or tag yet, so [onCategoryClick] and [onTagClick] default to doing nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesAndTagsSheet(
    categories: List<Category>,
    tags: List<Tag>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    // TODO: filter the Recipes list by category/tag once that is designed.
    onCategoryClick: (Category) -> Unit = {},
    onTagClick: (Tag) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        CategoriesAndTagsContent(
            categories = categories,
            tags = tags,
            onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
            onCategoryClick = onCategoryClick,
            onTagClick = onTagClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesAndTagsContent(
    categories: List<Category>,
    tags: List<Tag>,
    onClose: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 4.dp)
        ) {
            Text("Categories and tags", style = MaterialTheme.typography.titleLarge)
            Text(
                "Tap one to see every recipe that has it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
        ) {
            if (categories.isNotEmpty()) {
                ChipSection("Categories") {
                    categories.forEach {
                        CategoryTagChip(it.name, CategoryTagChipKind.Category, 40.dp) { onCategoryClick(it) }
                    }
                }
            }
            if (tags.isNotEmpty()) {
                ChipSection("Tags") {
                    tags.forEach {
                        CategoryTagChip(it.name, CategoryTagChipKind.Tag, 40.dp) { onTagClick(it) }
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
        ) {
            TextButton(onClick = onClose) { Text("Close") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(title: String, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { chips() }
    }
}

@Preview
@Composable
private fun CategoriesAndTagsContentPreview() {
    SimmerlyTheme {
        Surface {
            CategoriesAndTagsContent(
                categories = listOf("Main Course", "Dinner").mapIndexed { i, n -> Category("c$i", "g", n) },
                tags = listOf("Chicken", "Sheet Pan", "Weeknight", "Gluten Free", "High Protein", "Meal Prep", "Healthy")
                    .mapIndexed { i, n -> Tag("t$i", "g", n) },
                onClose = {}, onCategoryClick = {}, onTagClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun CategoriesAndTagsContentFewPreview() {
    SimmerlyTheme {
        Surface {
            CategoriesAndTagsContent(
                categories = emptyList(),
                tags = listOf(Tag("t1", "g", "Pasta")),
                onClose = {}, onCategoryClick = {}, onTagClick = {},
            )
        }
    }
}
