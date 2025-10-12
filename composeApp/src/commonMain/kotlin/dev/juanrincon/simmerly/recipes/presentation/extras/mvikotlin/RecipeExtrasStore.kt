package dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.presentation.extras.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.Label
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.State

interface RecipeExtrasStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    data class State(
        val comments: List<CommentUi> = emptyList(),
    )

    sealed interface Label {
    }
}
