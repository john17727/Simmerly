package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

@Composable
internal fun SelectableList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    selected: String,
    onRecipeSelected: (String) -> Unit,
    onSelected: (String) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (isLoading && recipes.isEmpty()) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(recipes, key = { it.id }) { item ->
                val isSelected = item.id == selected
                RecipeCard(
                    item,
                    selected = isSelected,
                    onClick = {
                        onRecipeSelected(item.id)
                        onSelected(item.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem().ifTrue(isSelected) {
                            padding(vertical = 32.dp)
                        }
                )
            }
            if (isLoading) {
                item {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
