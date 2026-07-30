package dev.juanrincon.simmerly.recipes.presentation.comments.orbit

sealed interface RecipeCommentsIntent {
    data class OnCommentTextChanged(val text: String) : RecipeCommentsIntent
    data object OnSendCommentClicked : RecipeCommentsIntent
}
