package dev.juanrincon.simmerly.recipes.presentation.comments.orbit

import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi

data class RecipeCommentsState(
    val comments: List<CommentUi> = emptyList(),
    val commentText: String = "",
)
