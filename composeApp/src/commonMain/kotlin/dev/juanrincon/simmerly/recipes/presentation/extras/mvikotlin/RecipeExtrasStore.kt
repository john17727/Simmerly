package dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.presentation.extras.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.Label
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore.State

interface RecipeExtrasStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    sealed interface State {
        data class Comments(val comments: List<CommentUi>): State
        data class Settings(val settings: Boolean): State
    }

    sealed interface Label {
    }
}
