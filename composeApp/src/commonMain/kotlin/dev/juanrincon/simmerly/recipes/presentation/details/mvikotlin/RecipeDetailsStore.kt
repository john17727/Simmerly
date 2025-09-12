package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State

interface RecipeDetailsStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    sealed interface State {
        data object Loading : State
        data class Content(val recipe: RecipeDetail) : State
    }

    sealed interface Label {
    }
}
