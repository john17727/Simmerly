package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.juanrincon.simmerly.recipes.presentation.list.RecipesScreen

@Composable
fun RecipesContent(component: RecipesComponent) {
    val state by component.state.collectAsState()
    RecipesScreen(state = state, onEvent = component::onEvent)
}