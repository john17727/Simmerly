package dev.juanrincon.simmerly.recipes.presentation.extras.decompose

import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore
import kotlinx.coroutines.flow.StateFlow

interface RecipeCommentsComponent {
    val state: StateFlow<RecipeExtrasStore.State>

    fun onEvent(event: RecipeExtrasStore.Intent)
}