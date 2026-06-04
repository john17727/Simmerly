package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
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
        RecipeList(
            lazyPagingItems = lazyPagingItems,
            onRecipeSelected = onRecipeSelected,
            sharedTransitionScope = sharedTransitionScope,
            lazyListState = lazyListState,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun RecipeList(
    lazyPagingItems: LazyPagingItems<RecipeSummary>,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(
                count = lazyPagingItems.itemCount,
                key = { index -> lazyPagingItems.peek(index)?.id ?: index }
            ) { index ->
                val item = lazyPagingItems[index] ?: return@items
                RecipeCard(
                    item,
                    onClick = { onRecipeSelected(item.id) },
                    sharedTransitionScope = sharedTransitionScope,
                    modifier = Modifier.fillMaxWidth().animateItem()
                )
            }
            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                item {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
