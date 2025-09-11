package dev.juanrincon.simmerly.recipes.presentation.details.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsScreen

@Composable
fun RecipeDetailsContent(component: RecipeDetailsComponent) {
    val state by component.state.collectAsState()
    RecipeDetailsScreen(state = state, onEvent = component::onEvent)
}