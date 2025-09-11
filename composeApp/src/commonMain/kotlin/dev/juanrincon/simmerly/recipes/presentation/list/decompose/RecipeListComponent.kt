package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipeListComponent {
    val state: StateFlow<RecipeListStore.State>

    val labels: Flow<RecipeListStore.Label>

    fun onEvent(event: RecipeListStore.Intent)
}