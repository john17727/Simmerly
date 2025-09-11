package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListScreen

@Composable
fun RecipeListContent(component: RecipeListComponent) {
    val state by component.state.collectAsState()
    RecipeListScreen(
        state = state,
        onEvent = component::onEvent,
        onOutput = component::onOutput,
        modifier = Modifier.fillMaxSize()
    )
}