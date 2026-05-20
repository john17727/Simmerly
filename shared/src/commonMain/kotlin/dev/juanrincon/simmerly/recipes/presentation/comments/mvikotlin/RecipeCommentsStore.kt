package dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.State

interface RecipeCommentsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class OnCommentTextChanged(val text: String) : Intent
        object OnSendCommentClicked : Intent
    }

    data class State(
        val comments: List<CommentUi> = emptyList(),
        val commentText: String = "",
    )

    sealed interface Label {
    }
}
