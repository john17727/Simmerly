package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListScreen
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecipeListContent(
    onRecipeSelected: (recipeId: String) -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    RecipeListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onRecipeSelected = onRecipeSelected,
        modifier = modifier
    )
}