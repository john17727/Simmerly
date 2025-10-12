package dev.juanrincon.simmerly.recipes.presentation.comments.models

data class CommentUi(
    val id: String,
    val text: String,
    val author: String,
    val date: String,
    val isAuthor: Boolean,
    val image: String
)
