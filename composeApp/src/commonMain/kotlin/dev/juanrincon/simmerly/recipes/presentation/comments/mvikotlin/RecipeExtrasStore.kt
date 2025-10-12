package dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeExtrasStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeExtrasStore.Label
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeExtrasStore.State

interface RecipeExtrasStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    data class State(
        val comments: List<CommentUi> = emptyList(),
    )

    sealed interface Label {
    }
}
