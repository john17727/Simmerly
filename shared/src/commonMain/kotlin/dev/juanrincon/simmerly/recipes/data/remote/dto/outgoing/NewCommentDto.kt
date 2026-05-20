package dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

@Serializable
data class NewCommentDto(
    val recipeId: String,
    val text: String
)
