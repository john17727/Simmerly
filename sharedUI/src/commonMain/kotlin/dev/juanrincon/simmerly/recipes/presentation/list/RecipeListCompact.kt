package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactRecipeList(
    lazyPagingItems: LazyPagingItems<RecipeSummary>,
    onSearchClicked: () -> Unit,
    onRecipeSelected: (String) -> Unit,
    onFavoriteToggled: (recipeId: String, favorite: Boolean) -> Unit,
    lazyGridState: LazyGridState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Recipes") },
                actions = {
                    IconButton(onClick = onSearchClicked) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        // Pull-to-refresh always asks the server, whatever the list's age; opening the screen only
        // refreshes a stale list.
        PullToRefreshBox(
            isRefreshing = lazyPagingItems.isRefreshingExistingItems,
            onRefresh = lazyPagingItems::refresh,
            // The caller's modifier is already on the Scaffold; applying it again doubled it up.
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            RecipeList(
                lazyPagingItems = lazyPagingItems,
                onRecipeSelected = onRecipeSelected,
                onFavoriteToggled = onFavoriteToggled,
                sharedTransitionScope = sharedTransitionScope,
                lazyGridState = lazyGridState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun RecipeList(
    lazyPagingItems: LazyPagingItems<RecipeSummary>,
    onRecipeSelected: (String) -> Unit,
    onFavoriteToggled: (recipeId: String, favorite: Boolean) -> Unit,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        state = lazyGridState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        // Skeletons are for the first load only; a refresh over existing items keeps them (and the
        // scroll position) instead of swapping the whole grid out.
        if (lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0) {
            items(6, key = { "skeleton-$it" }, contentType = { "skeleton" }) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(
                count = lazyPagingItems.itemCount,
                key = { index -> lazyPagingItems.peek(index)?.id ?: "placeholder-$index" },
                contentType = { "recipe" }
            ) { index ->
                val item = lazyPagingItems[index] ?: return@items
                RecipeCard(
                    item,
                    onClick = { onRecipeSelected(item.id) },
                    onFavoriteClick = { onFavoriteToggled(item.id, it) },
                    sharedTransitionScope = sharedTransitionScope,
                    modifier = Modifier.fillMaxWidth().animateItem()
                )
            }
            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                item(key = "append-skeleton", span = { GridItemSpan(maxLineSpan) }, contentType = "skeleton") {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
