package dev.juanrincon.simmerly.recipes.presentation.details.decompose

import dev.juanrincon.simmerly.core.presentation.AppBarAction
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipeDetailsComponent {

    val state: StateFlow<State>

    val labels: Flow<Label>

    fun onEvent(event: Intent)

    val actions: List<AppBarAction>
}