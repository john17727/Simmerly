package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore

@Composable
fun RecipesScreen(
    state: RecipesStore.State,
    onEvent: (RecipesStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyPagingItems = state.recipes.collectAsLazyPagingItems()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Coming Soon")
    }
}