package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val recipeId: String,
    val text: String,
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val userId: String,
    val user: UserDto
)