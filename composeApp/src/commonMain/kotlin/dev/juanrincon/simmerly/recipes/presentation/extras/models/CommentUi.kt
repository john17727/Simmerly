package dev.juanrincon.simmerly.recipes.presentation.extras.models

data class CommentUi(
    val id: String,
    val text: String,
    val user: String,
    val isUserAuthor: Boolean
)
