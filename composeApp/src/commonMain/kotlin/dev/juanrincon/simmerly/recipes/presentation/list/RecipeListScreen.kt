package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    state: RecipeListStore.State,
    onEvent: (RecipeListStore.Intent) -> Unit,
    onOutput: (RecipeListStore.Output) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                // Check if the last visible item is near the end of the list
                // A buffer of 5 items is used to load data before the user reaches the absolute end
                if (lastVisibleItemIndex != null && lastVisibleItemIndex >= state.recipes.size - 5 && !state.isLoading) {
                    onEvent(RecipeListStore.Intent.OnLoadMore)
                }
            }
    }
    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        SelectableList(
            state.recipes,
            selected = state.selectedRecipeId,
            onOutput = onOutput,
            onSelected = { onEvent(RecipeListStore.Intent.OnRecipeSelected(it)) },
            state = lazyListState,
            modifier = modifier
        )
    } else {
        List(
            state.recipes,
            onOutput,
            lazyListState,
            modifier = modifier
        )
    }
}

@Composable
fun SelectableList(
    recipes: List<RecipeSummary>,
    selected: String,
    onOutput: (RecipeListStore.Output) -> Unit,
    onSelected: (String) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        items(recipes) { item ->
            RecipeCard(
                item,
                selected = item.id == selected,
                onClick = {
                    onOutput(RecipeListStore.Output.SelectedRecipe(item.id))
                    onSelected(item.id)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun List(
    recipes: List<RecipeSummary>,
    onOutput: (RecipeListStore.Output) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        items(recipes) { item ->
            RecipeCard(
                item,
                onClick = { onOutput(RecipeListStore.Output.SelectedRecipe(item.id)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp).clip(MaterialTheme.shapes.medium)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    recipe.rating?.let {
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
                    recipe.cookTime?.let {
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
                }
            }
        }
    }
}
