package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListScreen

@Composable
fun RecipesListContent(component: RecipeListComponent) {
    val state by component.state.collectAsState()
    RecipeListScreen(state = state, onEvent = component::onEvent)
}