package dev.juanrincon.simmerly.recipes.presentation.comments.decompose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsScreen

@Composable
fun RecipeCommentsContent(component: RecipeCommentsComponent) {
    val state by component.state.collectAsState()

    RecipeCommentsScreen(
        state = state,
        onEvent = component::onEvent,
        modifier = Modifier.fillMaxSize()
    )
}