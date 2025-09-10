package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipesComponent {
    val state: StateFlow<RecipesStore.State>

    val labels: Flow<RecipesStore.Label>

    fun onEvent(event: RecipesStore.Intent)
}