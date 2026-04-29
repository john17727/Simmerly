package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactRecipeList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Recipes") },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        RecipeList(
            recipes = recipes,
            isLoading = isLoading,
            onRecipeSelected = onRecipeSelected,
            lazyListState = lazyListState,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun RecipeList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (isLoading && recipes.isEmpty()) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(recipes, key = { it.id }) { item ->
                RecipeCard(
                    item,
                    onClick = { onRecipeSelected(item.id) },
                    modifier = Modifier.fillMaxWidth().animateItem()
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
