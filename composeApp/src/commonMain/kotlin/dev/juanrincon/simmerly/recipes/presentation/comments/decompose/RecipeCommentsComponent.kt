package dev.juanrincon.simmerly.recipes.presentation.comments.decompose

import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore
import kotlinx.coroutines.flow.StateFlow

interface RecipeCommentsComponent {
    val state: StateFlow<RecipeCommentsStore.State>

    fun onEvent(event: RecipeCommentsStore.Intent)
}