package dev.juanrincon.simmerly.recipes.presentation.details.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsScreen
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RecipeDetailsContent(
    recipeId: String,
    viewModel: RecipeDetailsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    RecipeDetailsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}